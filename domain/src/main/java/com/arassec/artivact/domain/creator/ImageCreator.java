package com.arassec.artivact.domain.creator;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.model.item.Asset;
import com.arassec.artivact.core.model.item.CreationImageSet;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.creator.adapter.Adapter;
import com.arassec.artivact.domain.creator.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.domain.creator.adapter.image.background.BackgroundRemovalInitParams;
import com.arassec.artivact.domain.creator.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.domain.creator.adapter.image.camera.CameraInitParams;
import com.arassec.artivact.domain.creator.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.domain.creator.adapter.image.turntable.TurntableInitParams;
import com.arassec.artivact.domain.creator.util.DirectoryWatcher;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

/**
 * Service for image handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCreator extends BaseCreator {

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Provides the project's root directory.
     */
    @Getter
    private final ProjectDataProvider projectDataProvider;

    /**
     * Watches for new images in the filesystem during photo capturing.
     */
    private final DirectoryWatcher directoryWatcher;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * List of all available adapters.
     */
    private final List<Adapter<?, ?>> adapters;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * Captures photos using adapters and adds them to a new image set of the currently active item.
     *
     * @param itemId            The ID of the item to capture photos for.
     * @param numPhotos         The number of photos to capture.
     * @param useTurnTable      {@code true} if a turntable should be used.
     * @param turnTableDelay    Delay of the turntable communication.
     * @param removeBackgrounds Automatically removes backgrounds from newly captured images when set to {@code true}.
     * @param progressMonitor   The progress monitor which is updated during processing.
     * @return The {@link CreationImageSet} containing the images.
     */
    public List<CreationImageSet> capturePhotos(String itemId, int numPhotos, boolean useTurnTable,
                                                int turnTableDelay, boolean removeBackgrounds,
                                                ProgressMonitor progressMonitor) {

        List<CreationImageSet> result = new LinkedList<>();
        CreationImageSet captureResult = new CreationImageSet();

        Path targetDir = getImagesDir(itemId, true);
        fileRepository.createDirIfRequired(targetDir);

        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();

        // Initialize adapters:
        TurntableAdapter turntableAdapter = getTurntableAdapter(adapterConfiguration);
        if (useTurnTable) {
            log.debug("Initializing turntable adapter for photo capturing: {}", turntableAdapter.getSupportedImplementation());
            turntableAdapter.initialize(progressMonitor, TurntableInitParams.builder()
                    .turntableDelay(turnTableDelay)
                    .build());
        }

        CameraAdapter cameraAdapter = getCameraAdapter(adapterConfiguration);
        log.debug("Initializing camera adapter for photo capturing: {}", cameraAdapter.getSupportedImplementation());
        cameraAdapter.initialize(progressMonitor, CameraInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(targetDir)
                .build());

        BackgroundRemovalAdapter backgroundRemovalAdapter = getBackgroundRemovalAdapter(adapterConfiguration);
        log.debug("Initializing background-removal adapter for photo capturing: {}", backgroundRemovalAdapter.getSupportedImplementation());
        backgroundRemovalAdapter.initialize(progressMonitor, BackgroundRemovalInitParams.builder()
                .projectRoot(projectDataProvider.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

        log.debug("Starting capturing of images.");

        // Prepare directory watcher to detect newly captured photos:
        List<Path> capturedImages = Collections.synchronizedList(new LinkedList<>());
        directoryWatcher.startWatching(targetDir, numPhotos,
                newImage -> processNewImage(removeBackgrounds, backgroundRemovalAdapter, newImage, capturedImages));

        progressMonitor.updateLabelKey("captureInProgress");

        // Start capturing:
        for (var i = 0; i < numPhotos; i++) {
            progressMonitor.updateProgress((i + 1), numPhotos);
            String filename = getAssetName(getNextAssetNumber(targetDir), null);
            log.debug("Capturing image: {}", filename);

            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> cameraAdapter.captureImage(filename));

            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos);
            }
        }

        // Finished capturing photos. Wait for all images to be detected and stop watching for new files.
        directoryWatcher.finishWatching(2500);

        // Add new images to active item:
        addCapturedImagesToImageSet(itemId, capturedImages, progressMonitor, captureResult);
        captureResult.setModelInput(!removeBackgrounds);
        result.add(captureResult);

        // Teardown adapters:
        if (useTurnTable) {
            turntableAdapter.teardown();
        }
        cameraAdapter.teardown();
        Optional<List<Path>> imagesWithoutBackground = backgroundRemovalAdapter.teardown();
        if (removeBackgrounds && imagesWithoutBackground.isPresent()) {
            CreationImageSet bgRemovalResult = CreationImageSet.builder()
                    .modelInput(true)
                    .build();
            addCapturedImagesToImageSet(itemId, imagesWithoutBackground.get(), progressMonitor, bgRemovalResult);
            result.add(bgRemovalResult);
        }

        return result;
    }

    /**
     * Removes backgrounds from all images in the image set with the given index.
     *
     * @param itemId           The ID of the item to which the images belong.
     * @param creationImageSet The image-set to process images from.
     * @param progressMonitor  The progress monitor which is updated during processing.
     * @return List of paths of newly created images without background.
     */
    public List<Path> removeBackgrounds(String itemId, CreationImageSet creationImageSet, ProgressMonitor progressMonitor) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        BackgroundRemovalAdapter backgroundRemovalAdapter = getBackgroundRemovalAdapter(adapterConfiguration);

        backgroundRemovalAdapter.initialize(progressMonitor, BackgroundRemovalInitParams.builder()
                .projectRoot(projectDataProvider.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

        progressMonitor.updateProgress(0, creationImageSet.getFiles().size());

        backgroundRemovalAdapter.removeBackgrounds(creationImageSet.getFiles().stream()
                .map(fileName -> getImagesDir(itemId, true).resolve(fileName))
                .toList());

        return backgroundRemovalAdapter.teardown().orElseGet(List::of);
    }

    public Path getTransferSourcePath(String itemId, Asset image) {
        return getImagesDir(itemId, true).resolve(image.getFileName());
    }

    public Path getTransferTargetPath(String itemId, Asset image) {
        Path imagesDir = getImagesDir(itemId, true);
        int nextAssetNumber = getNextAssetNumber(imagesDir);
        String extension = FilenameUtils.getExtension(image.getFileName());
        return projectDataProvider.getProjectRoot().resolve(getImagePath(itemId, nextAssetNumber, extension));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link TurntableAdapter}.
     */
    private TurntableAdapter getTurntableAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getTurntableAdapterImplementation();
        return adapters.stream()
                .filter(TurntableAdapter.class::isInstance)
                .map(TurntableAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException(NO_ADAPTER_ERROR));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link CameraAdapter}.
     */
    private CameraAdapter getCameraAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getCameraAdapterImplementation();
        return adapters.stream()
                .filter(CameraAdapter.class::isInstance)
                .map(CameraAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException(NO_ADAPTER_ERROR));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link BackgroundRemovalAdapter}.
     */
    private BackgroundRemovalAdapter getBackgroundRemovalAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getBackgroundRemovalAdapterImplementation();
        return adapters.stream()
                .filter(BackgroundRemovalAdapter.class::isInstance)
                .map(BackgroundRemovalAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException(NO_ADAPTER_ERROR));
    }

    /**
     * Adds internally captured images to an existing image set of the currently active item.
     *
     * @param images                 The images to add to the set.
     * @param progressMonitor        The progress monitor which is updated during processing.
     * @param targetCreationImageSet The target image set to add the images to.
     */
    private void addCapturedImagesToImageSet(String itemId, List<Path> images, ProgressMonitor progressMonitor, CreationImageSet targetCreationImageSet) {
        if (images != null && !images.isEmpty() && targetCreationImageSet != null) {

            progressMonitor.updateLabelKey("imageSetInProgress");

            List<String> filenames = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                filenames.add(addImage(itemId, image));
                progressMonitor.updateProgress(index.addAndGet(1), images.size());
            });
            targetCreationImageSet.getFiles().addAll(filenames);
        }
    }

    /**
     * Processes a newly created image file. Other than "JPEG" files are ignored.
     *
     * @param removeBackgrounds        Set to {@code true} to directly remove backgrounds of newly found images.
     * @param backgroundRemovalAdapter The background removal adapter to use.
     * @param createdFile              The newly found file.
     * @param capturedImages           Target list where newly processed images will be stored in.
     */
    private void processNewImage(boolean removeBackgrounds, BackgroundRemovalAdapter backgroundRemovalAdapter,
                                 Path createdFile, List<Path> capturedImages) {
        String filename = createdFile.toString().toLowerCase();
        // Check filetype and if ready for processing. The image might not be completely written by the camera adapter!
        if ((filename.endsWith(".jpg") || filename.endsWith(".jpeg")) && readyForProcessing(createdFile)) {
            log.debug("Captured image file found: {}", createdFile);
            capturedImages.add(createdFile);
            // Check again if ready for processing, the file might still be blocked by another process!
            if (removeBackgrounds && readyForProcessing(createdFile)) {
                log.debug("Removing Background of captured image: {}", createdFile);
                backgroundRemovalAdapter.removeBackground(createdFile);
            }
        }
    }

    /**
     * Returns the path to an image with the given asset number.
     *
     * @param itemId      ID of the item to get the image path from.
     * @param assetNumber The image's asset number.
     * @param extension   The image's file extension.
     * @return Path to the image file.
     */
    private Path getImagePath(String itemId, int assetNumber, String extension) {
        var firstSubDir = getSubDir(itemId, 0);
        var secondSubDir = getSubDir(itemId, 1);
        var imageName = getAssetName(assetNumber, extension);
        return Path.of(ProjectDataProvider.ITEMS_DIR, firstSubDir, secondSubDir, itemId, ProjectDataProvider.IMAGES_DIR, imageName);
    }

    /**
     * Adds an image to the provided item.
     *
     * @param itemId The ID of the item to add the image to.
     * @param image  The image to add.
     * @return The newly created image's filename.
     */
    private String addImage(String itemId, Path image) {
        log.debug("Adding new image: {}", image);

        String[] assetNameParts = image.getFileName().toString().split("\\.");
        int assetNumber = Integer.parseInt(assetNameParts[0]);
        var extension = "";
        if (assetNameParts.length > 1) {
            extension = assetNameParts[assetNameParts.length - 1];
        }

        var targetFile = getImagePath(itemId, assetNumber, extension);

        return targetFile.getFileName().toString();
    }

    /**
     * Checks if the given file can be accessed.
     *
     * @param file The file to check.
     * @return {@code true} if the file can be processed, {@code false} otherwise.
     */
    private boolean readyForProcessing(Path file) {
        log.debug("Checking file to ensure write access for further processing: {}", file);
        int iterations = 100;
        int i = 0;
        while (i < iterations) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactException("Interrupted during wait for file check!", e);
            }
            i++;
            if (!Files.exists(file)) {
                continue;
            }
            try (RandomAccessFile raFile = new RandomAccessFile(file.toAbsolutePath().toString(), "rw")) {
                return raFile.getFD() != null;
            } catch (IOException e) {
                log.debug("File not ready for processing ({})!", i);
            }
        }
        return false;
    }

}
