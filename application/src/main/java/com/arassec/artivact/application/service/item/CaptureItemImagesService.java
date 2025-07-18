package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadAdapterConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CaptureItemImagesUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulationPeripheral;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.item.util.DirectoryWatcher;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptureItemImagesService implements CaptureItemImagesUseCase {

    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final FileRepository fileRepository;

    private final LoadAdapterConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available adapters.
     */
    private final List<PeripheralAdapter> peripheralAdapters;

    /**
     * Watches for new images in the filesystem during photo capturing.
     */
    private final DirectoryWatcher directoryWatcher;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void capture(String itemId, CaptureImagesParams captureImagesParams) {
        runBackgroundOperationUseCase.execute("captureImages", "start", progressMonitor -> {
            List<CreationImageSet> creationImageSets = captureImages(itemId, captureImagesParams, progressMonitor);

            Item item = loadItemUseCase.loadTranslated(itemId);
            item.getMediaCreationContent().getImageSets().addAll(creationImageSets);
            saveItemUseCase.save(item);

            // Sleep some time to avoid frontend update without recognizing the new image set(s)...
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.error("Error during photo capturing!", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    private List<CreationImageSet> captureImages(String itemId, CaptureImagesParams captureImagesParams, ProgressMonitor progressMonitor) {

        List<CreationImageSet> result = new LinkedList<>();
        CreationImageSet captureResult = new CreationImageSet();

        Path targetDir = useProjectDirsUseCase.getImagesDir(itemId);
        fileRepository.createDirIfRequired(targetDir);

        AdapterConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadAdapterConfiguration();

        boolean useTurnTable = captureImagesParams.isUseTurnTable();
        boolean removeBackgrounds = captureImagesParams.isRemoveBackgrounds();
        int numPhotos = captureImagesParams.getNumPhotos();

        TurntablePeripheral turntableAdapter = getPeripheralAdapter(adapterConfiguration.getTurntableAdapterImplementation(), TurntablePeripheral.class);
        if (useTurnTable) {
            log.debug("Initializing turntable adapter for image capturing: {}", turntableAdapter.getSupportedImplementation());
            turntableAdapter.initialize(progressMonitor, PeripheralAdapterInitParams.builder().build());
        }

        CameraPeripheral cameraAdapter = getPeripheralAdapter(adapterConfiguration.getCameraAdapterImplementation(), CameraPeripheral.class);
        log.debug("Initializing camera adapter for image capturing: {}", cameraAdapter.getSupportedImplementation());
        cameraAdapter.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .workDir(targetDir)
                .build());

        ImageManipulationPeripheral imageManipulationAdapter = getPeripheralAdapter(adapterConfiguration.getImageManipulationAdapterImplementation(), ImageManipulationPeripheral.class);
        log.debug("Initializing iamge-manipulation adapter for image capturing: {}", imageManipulationAdapter.getSupportedImplementation());
        imageManipulationAdapter.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .workDir(useProjectDirsUseCase.getImagesDir(itemId))
                .build());

        log.debug("Starting capturing of images.");

        // Prepare the directory watcher to detect newly captured photos:
        List<Path> capturedImages = Collections.synchronizedList(new LinkedList<>());
        directoryWatcher.startWatching(targetDir, numPhotos,
                newImage -> processNewImage(removeBackgrounds, imageManipulationAdapter, newImage, capturedImages));

        progressMonitor.updateLabelKey("inProgress");

        // Start capturing:
        for (var i = 0; i < numPhotos; i++) {
            progressMonitor.updateProgress((i + 1), numPhotos);
            String filename = fileRepository.getAssetName(fileRepository.getNextAssetNumber(targetDir), null);
            log.debug("Capturing image: {}", filename);

            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> cameraAdapter.captureImage(filename));

            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos, captureImagesParams.getTurnTableDelay());
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
        imageManipulationAdapter.teardown();

        List<Path> imagesWithoutBackground = imageManipulationAdapter.getModifiedImages();
        if (removeBackgrounds && !imagesWithoutBackground.isEmpty()) {
            CreationImageSet bgRemovalResult = CreationImageSet.builder()
                    .modelInput(true)
                    .build();
            addCapturedImagesToImageSet(itemId, imagesWithoutBackground, progressMonitor, bgRemovalResult);
            result.add(bgRemovalResult);
        }

        return result;
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterImplementation The peripheral adapter implementation to use.
     * @param clazz                 The peripheral adapter implementing class.
     * @return The configured {@link TurntablePeripheral}.
     */
    private <T extends PeripheralAdapter> T getPeripheralAdapter(AdapterImplementation adapterImplementation, Class<T> clazz) {
        return peripheralAdapters.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Processes a newly created image file. Other than "JPEG" files are ignored.
     *
     * @param removeBackgrounds        Set to {@code true} to directly remove backgrounds of newly found images.
     * @param imageManipulationAdapter The background removal adapter to use.
     * @param createdFile              The newly found file.
     * @param capturedImages           Target list where newly processed images will be stored in.
     */
    private void processNewImage(boolean removeBackgrounds, ImageManipulationPeripheral imageManipulationAdapter,
                                 Path createdFile, List<Path> capturedImages) {
        String filename = createdFile.toString().toLowerCase();
        // Check filetype and if ready for processing. The image might not be completely written by the camera adapter!
        if ((filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) && readyForProcessing(createdFile)) {
            log.debug("Captured image file found: {}", createdFile);
            capturedImages.add(createdFile);
            // Check again if ready for processing, the file might still be blocked by another process!
            if (removeBackgrounds && readyForProcessing(createdFile)) {
                log.debug("Removing Background of captured image: {}", createdFile);
                imageManipulationAdapter.removeBackground(createdFile);
            }
        }
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

        var imageName = fileRepository.getAssetName(assetNumber, extension);

        var targetFile = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getItemsDir(), itemId, DirectoryDefinitions.IMAGES_DIR)
                .resolve(imageName);

        return targetFile.getFileName().toString();
    }

}
