package com.arassec.artivact.backend.service.creator;

import com.arassec.artivact.backend.api.model.Asset;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.creator.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.background.BackgroundRemovalInitParams;
import com.arassec.artivact.backend.service.creator.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.camera.CameraInitParams;
import com.arassec.artivact.backend.service.creator.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.turntable.TurntableInitParams;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.ProjectDir;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.CreationImageSet;
import com.arassec.artivact.backend.service.util.DirectoryWatcher;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Message source for I18N.
     */
    private final MessageSource messageSource;

    /**
     * Provides the project's root directory.
     */
    @Getter
    private final ProjectRootProvider projectRootProvider;

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
    @Getter
    private final List<Adapter<?, ?>> adapters;

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

        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();

        // Initialize adapters:
        TurntableAdapter turntableAdapter = getTurntableAdapter(adapterConfiguration);
        log.debug("Initializing turntable adapter for photo capturing: {}", turntableAdapter.getSupportedImplementation());
        turntableAdapter.initialize(progressMonitor, TurntableInitParams.builder()
                .turntableDelay(turnTableDelay)
                .build());

        CameraAdapter cameraAdapter = getCameraAdapter(adapterConfiguration);
        log.debug("Initializing camera adapter for photo capturing: {}", cameraAdapter.getSupportedImplementation());
        cameraAdapter.initialize(progressMonitor, CameraInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(targetDir)
                .build());

        BackgroundRemovalAdapter backgroundRemovalAdapter = getBackgroundRemovalAdapter(adapterConfiguration);
        log.debug("Initializing background-removal adapter for photo capturing: {}", backgroundRemovalAdapter.getSupportedImplementation());
        backgroundRemovalAdapter.initialize(progressMonitor, BackgroundRemovalInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

        log.debug("Starting capturing of images.");

        // Prepare directory watcher to detect newly captured photos:
        List<Path> capturedImages = Collections.synchronizedList(new LinkedList<>());
        directoryWatcher.startWatching(targetDir, numPhotos,
                newImage -> processNewImage(removeBackgrounds, backgroundRemovalAdapter, newImage, capturedImages));

        String progressPrefix = messageSource.getMessage("image-creator.capture-photos.progress.prefix", null, Locale.getDefault());

        // Start capturing:
        for (var i = 0; i < numPhotos; i++) {
            log.debug("Capturing image: {}", i);
            progressMonitor.updateProgress(progressPrefix + " (" + (i + 1) + "/" + numPhotos + ")");
            String filename = getAssetName(getNextAssetNumber(targetDir), null);
            cameraAdapter.captureImage(filename);
            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos);
            }
        }

        // Finished capturing photos. Wait for all images to be detected and stop watching for new files.
        directoryWatcher.finishWatching(2500);

        // Add new images to active item:
        addCapturedImagesToImageSet(itemId, capturedImages, progressMonitor, captureResult);
        result.add(captureResult);

        // Teardown adapters:
        turntableAdapter.teardown();
        cameraAdapter.teardown();
        Optional<List<Path>> imagesWithoutBackground = backgroundRemovalAdapter.teardown();
        if (removeBackgrounds && imagesWithoutBackground.isPresent()) {
            CreationImageSet bgRemovalResult = new CreationImageSet();
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
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

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
        return projectRootProvider.getProjectRoot().resolve(getImagePath(itemId, nextAssetNumber, extension));
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
            String progressPrefix = messageSource.getMessage("image-creator.add-images-to-set.progress.prefix", null, Locale.getDefault());

            List<String> fileNames = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                fileNames.add(addImage(itemId, image));
                progressMonitor.updateProgress(progressPrefix + " (" + index.addAndGet(1) + "/" + images.size() + ")");
            });
            targetCreationImageSet.getFiles().addAll(fileNames);
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
        if (createdFile.toString().toLowerCase().endsWith(".jpg") || createdFile.toString().toLowerCase().endsWith(".jpeg")) {
            int iterations = 100;
            int i = 0;
            while (!Files.exists(createdFile) && i < iterations) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ArtivactException("Interrupted during processing of newly created image file!", e);
                }
                i++;
            }
            log.debug("Captured image file found: {}", createdFile);

            capturedImages.add(createdFile);

            if (removeBackgrounds) {
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
        return Path.of(ProjectDir.ITEMS_DIR, firstSubDir, secondSubDir, itemId, ProjectDir.IMAGES_DIR, imageName);
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

}
