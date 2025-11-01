package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.CaptureItemImageUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulatorPeripheral;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
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
     * {@inheritDoc
     */
    @Override
    public String captureImage(String itemId, CaptureImagesParams captureImagesParams) {
        PeripheralsConfiguration peripheralsConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        ProgressMonitor progressMonitor = new ProgressMonitor("captureTempImage", "start");

        PeripheralConfig cameraPeripheralConfig = peripheralsConfiguration.getCameraPeripheralConfigs().stream()
                .filter(config -> config.getId().equals(captureImagesParams.getCameraPeripheralConfigId()))
                .findFirst()
                .orElseThrow();

        CameraPeripheral cameraPeripheral = getPeripheral(cameraPeripheralConfig.getPeripheralImplementation(), CameraPeripheral.class);
        log.debug("Initializing camera peripheral for temp image capturing: {}", cameraPeripheral.getSupportedImplementation());
        cameraPeripheral.initialize(progressMonitor, PeripheralInitParams.builder()
                .config(cameraPeripheralConfig)
                .build());

        ImageManipulatorPeripheral imageManipulatorPeripheral = null;
        if (captureImagesParams.isRemoveBackgrounds()) {
            PeripheralConfig imageManipulatorPeripheralConfig = peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs().stream()
                    .filter(config -> config.getId().equals(captureImagesParams.getImageBackgroundRemovalPeripheralConfigId()))
                    .findFirst()
                    .orElseThrow();

            imageManipulatorPeripheral = getPeripheral(imageManipulatorPeripheralConfig.getPeripheralImplementation(), ImageManipulatorPeripheral.class);
            log.debug("Initializing image-manipulation peripheral for temp image capturing: {}", imageManipulatorPeripheral.getSupportedImplementation());
            imageManipulatorPeripheral.initialize(progressMonitor, PeripheralInitParams.builder()
                    .projectRoot(useProjectDirsUseCase.getProjectRoot())
                    .config(imageManipulatorPeripheralConfig)
                    .workDir(useProjectDirsUseCase.getImagesDir(itemId))
                    .build());
        }

        Path targetDir = useProjectDirsUseCase.getImagesDir(itemId);
        fileRepository.createDirIfRequired(targetDir);

        String filename = fileRepository.getAssetName(fileRepository.getNextAssetNumber(targetDir), "jpg");
        Path targetFile = targetDir.resolve(filename).toAbsolutePath();

        boolean imageCaptured = cameraPeripheral.captureImage(targetFile);

        if (imageCaptured && captureImagesParams.isRemoveBackgrounds() && imageManipulatorPeripheral != null) {
            imageManipulatorPeripheral.removeBackground(targetFile);
        }

        cameraPeripheral.teardown();

        if (captureImagesParams.isRemoveBackgrounds() && imageManipulatorPeripheral != null) {
            imageManipulatorPeripheral.teardown();
            fileRepository.delete(targetFile);
            return imageManipulatorPeripheral.getModifiedImages().getFirst().getFileName().toString();
        }

        return targetFile.getFileName().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void captureImages(String itemId, CaptureImagesParams captureImagesParams) {
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

        PeripheralsConfiguration peripheralsConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        boolean useTurnTable = captureImagesParams.isUseTurnTable();
        boolean removeBackgrounds = captureImagesParams.isRemoveBackgrounds();
        int numPhotos = captureImagesParams.getNumPhotos();

        TurntablePeripheral turntablePeripheral = null;
        if (useTurnTable) {
            PeripheralConfig turntablePeripheralConfig = peripheralsConfiguration.getTurntablePeripheralConfigs().stream()
                    .filter(config -> config.getId().equals(captureImagesParams.getTurntablePeripheralConfigId()))
                    .findFirst()
                    .orElseThrow();

            turntablePeripheral = getPeripheral(turntablePeripheralConfig.getPeripheralImplementation(), TurntablePeripheral.class);
            log.debug("Initializing turntable adapter for image capturing: {}", turntablePeripheral.getSupportedImplementation());
            turntablePeripheral.initialize(progressMonitor, PeripheralInitParams.builder()
                    .config(turntablePeripheralConfig)
                    .build());
        }

        PeripheralConfig cameraPeripheralConfig = peripheralsConfiguration.getCameraPeripheralConfigs().stream()
                .filter(config -> config.getId().equals(captureImagesParams.getCameraPeripheralConfigId()))
                .findFirst()
                .orElseThrow();

        CameraPeripheral cameraPeripheral = getPeripheral(cameraPeripheralConfig.getPeripheralImplementation(), CameraPeripheral.class);
        log.debug("Initializing camera adapter for image capturing: {}", cameraPeripheral.getSupportedImplementation());
        cameraPeripheral.initialize(progressMonitor, PeripheralInitParams.builder()
                .config(cameraPeripheralConfig)
                .build());

        ImageManipulatorPeripheral imageManipulatorPeripheral = null;
        if (removeBackgrounds) {
            PeripheralConfig imageManipulatorPeripheralConfig = peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs().stream()
                    .filter(config -> config.getId().equals(captureImagesParams.getImageBackgroundRemovalPeripheralConfigId()))
                    .findFirst()
                    .orElseThrow();

            imageManipulatorPeripheral = getPeripheral(imageManipulatorPeripheralConfig.getPeripheralImplementation(), ImageManipulatorPeripheral.class);
            log.debug("Initializing image-manipulation adapter for image capturing: {}", imageManipulatorPeripheral.getSupportedImplementation());
            imageManipulatorPeripheral.initialize(progressMonitor, PeripheralInitParams.builder()
                    .projectRoot(useProjectDirsUseCase.getProjectRoot())
                    .config(imageManipulatorPeripheralConfig)
                    .workDir(useProjectDirsUseCase.getImagesDir(itemId))
                    .build());
        }


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
                imageManipulatorPeripheral.removeBackground(targetFile);
            }

            if (useTurnTable) {
                turntablePeripheral.rotate(numPhotos);
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

        // Teardown peripherals:
        if (useTurnTable) {
            turntablePeripheral.teardown();
        }
        cameraPeripheral.teardown();
        if (removeBackgrounds) {
            imageManipulatorPeripheral.teardown();
        }

        // Add manipulated images to active item:
        if (removeBackgrounds) {
            List<Path> imagesWithoutBackground = imageManipulatorPeripheral.getModifiedImages();
            if (!imagesWithoutBackground.isEmpty()) {
                creationImageSet = CreationImageSet.builder()
                        .backgroundRemoved(true)
                        .modelInput(true)
                        .build();
                creationImageSet.getFiles().addAll(imagesWithoutBackground.stream()
                        .map(image -> image.getFileName().toString())
                        .toList());
                result.add(creationImageSet);
            }
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
