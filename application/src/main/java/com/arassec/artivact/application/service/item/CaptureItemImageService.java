package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CaptureItemImageUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulationPeripheral;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralAdapterInitParams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the {@link CaptureItemImageUseCase}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptureItemImageService implements CaptureItemImageUseCase {

    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final FileRepository fileRepository;

    private final LoadPeripheralConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available adapters.
     */
    private final List<Peripheral> peripheralAdapters;

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
        });
    }

    /**
     * Captures images using various peripherals.
     *
     * @param itemId              The item's ID.
     * @param captureImagesParams Parameters for image capturing.
     * @param progressMonitor     The progress monitor.
     * @return List of newly created image sets that contain the captured images.
     */
    private List<CreationImageSet> captureImages(String itemId, CaptureImagesParams captureImagesParams, ProgressMonitor progressMonitor) {

        List<CreationImageSet> result = new LinkedList<>();

        Path targetDir = useProjectDirsUseCase.getImagesDir(itemId);
        fileRepository.createDirIfRequired(targetDir);

        PeripheralConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        boolean useTurnTable = captureImagesParams.isUseTurnTable();
        boolean removeBackgrounds = captureImagesParams.isRemoveBackgrounds();
        int numPhotos = captureImagesParams.getNumPhotos();

        TurntablePeripheral turntablePeripheral = getPeripheral(adapterConfiguration.getTurntablePeripheralImplementation(), TurntablePeripheral.class);
        if (useTurnTable) {
            log.debug("Initializing turntable adapter for image capturing: {}", turntablePeripheral.getSupportedImplementation());
            turntablePeripheral.initialize(progressMonitor, PeripheralAdapterInitParams.builder().build());
        }

        CameraPeripheral cameraPeripheral = getPeripheral(adapterConfiguration.getCameraPeripheralImplementation(), CameraPeripheral.class);
        log.debug("Initializing camera adapter for image capturing: {}", cameraPeripheral.getSupportedImplementation());
        cameraPeripheral.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .build());

        ImageManipulationPeripheral imageManipulationPeripheral = getPeripheral(adapterConfiguration.getImageManipulationPeripheralImplementation(), ImageManipulationPeripheral.class);
        log.debug("Initializing iamge-manipulation adapter for image capturing: {}", imageManipulationPeripheral.getSupportedImplementation());
        imageManipulationPeripheral.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .workDir(useProjectDirsUseCase.getImagesDir(itemId))
                .build());

        log.debug("Starting capturing of images.");

        // Prepare the directory watcher to detect newly captured photos:
        List<Path> capturedImages = new LinkedList<>();

        progressMonitor.updateLabelKey("inProgress");

        // Start capturing:
        for (var i = 0; i < numPhotos; i++) {
            progressMonitor.updateProgress((i + 1), numPhotos);

            String filename = fileRepository.getAssetName(fileRepository.getNextAssetNumber(targetDir), "jpg");
            Path targetFile = targetDir.resolve(filename).toAbsolutePath();
            log.debug("Capturing image: {}", targetFile);

            if (!cameraPeripheral.captureImage(targetFile)) {
                throw new ArtivactException("Could not capture image!");
            }

            capturedImages.add(targetFile);

            if (removeBackgrounds) {
                log.debug("Removing Background of captured image: {}", targetFile);
                imageManipulationPeripheral.removeBackground(targetFile);
            }

            if (useTurnTable) {
                turntablePeripheral.rotate(numPhotos, captureImagesParams.getTurnTableDelay());
            }
        }

        // Add new images to active item:
        CreationImageSet creationImageSet = CreationImageSet.builder()
                .backgroundRemoved(false)
                .modelInput(!removeBackgrounds)
                .build();
        creationImageSet.getFiles().addAll(capturedImages.stream()
                .map(image -> image.getFileName().toString())
                .toList());
        result.add(creationImageSet);

        // Teardown adapters:
        if (useTurnTable) {
            turntablePeripheral.teardown();
        }
        cameraPeripheral.teardown();
        imageManipulationPeripheral.teardown();

        // Add manipulated images to active item:
        List<Path> imagesWithoutBackground = imageManipulationPeripheral.getModifiedImages();
        if (removeBackgrounds && !imagesWithoutBackground.isEmpty()) {
            creationImageSet = CreationImageSet.builder()
                    .backgroundRemoved(true)
                    .modelInput(true)
                    .build();
            creationImageSet.getFiles().addAll(imagesWithoutBackground.stream()
                    .map(image -> image.getFileName().toString())
                    .toList());
            result.add(creationImageSet);
        }

        return result;
    }

    /**
     * Returns the desired peripheral.
     *
     * @param adapterImplementation The peripheral implementation to use.
     * @param clazz                 The peripheral implementing class.
     * @return The configured {@link TurntablePeripheral}.
     */
    private <T extends Peripheral> T getPeripheral(PeripheralImplementation adapterImplementation, Class<T> clazz) {
        return peripheralAdapters.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

}
