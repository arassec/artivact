package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.ManipulateItemImagesUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulatorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManipulateItemImagesService implements ManipulateItemImagesUseCase {

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    private final LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available adapters.
     */
    private final List<Peripheral> peripheralAdapters;

    @Override
    public synchronized void removeBackgrounds(String itemId, String imageManipulatorConfigId, int imageSetIndex) {
        runBackgroundOperationUseCase.execute("manipulateImage", "backgroundRemovalStart", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);

            List<Path> imagesWithoutBackground = removeBackgrounds(itemId, imageManipulatorConfigId, item.getMediaCreationContent().getImageSets().get(imageSetIndex), progressMonitor);

            if (!imagesWithoutBackground.isEmpty()) {
                item.getMediaCreationContent().getImageSets().add(CreationImageSet.builder()
                        .backgroundRemoved(true)
                        .modelInput(true)
                        .files(imagesWithoutBackground.stream()
                                .map(Path::getFileName)
                                .map(Path::toString)
                                .toList())
                        .build());

                saveItemUseCase.save(item);
            }
        });
    }

    /**
     * Removes backgrounds from all images in the image set with the given index.
     *
     * @param itemId           The ID of the item to which the images belong.
     * @param creationImageSet The image-set to process images from.
     * @param progressMonitor  The progress monitor which is updated during processing.
     * @return List of paths of newly created images without background.
     */
    private List<Path> removeBackgrounds(String itemId, String imageManipulatorConfigId, CreationImageSet creationImageSet, ProgressMonitor progressMonitor) {
        PeripheralsConfiguration peripheralsConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        PeripheralConfig imageManipulatorPeripheralConfig = peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs().stream()
                .filter(config -> config.getId().equals(imageManipulatorConfigId))
                .findFirst()
                .orElseThrow();

        ImageManipulatorPeripheral imageManipulationAdapter = peripheralAdapters.stream()
                .filter(ImageManipulatorPeripheral.class::isInstance)
                .map(ImageManipulatorPeripheral.class::cast)
                .filter(adapter -> adapter.supports(imageManipulatorPeripheralConfig.getPeripheralImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect image-manipulation adapter!"));

        imageManipulationAdapter.initialize(progressMonitor, PeripheralInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .config(imageManipulatorPeripheralConfig)
                .workDir(useProjectDirsUseCase.getImagesDir(itemId))
                .build());

        progressMonitor.updateProgress(0, creationImageSet.getFiles().size());

        imageManipulationAdapter.removeBackgrounds(creationImageSet.getFiles().stream()
                .map(fileName -> useProjectDirsUseCase.getImagesDir(itemId).resolve(fileName))
                .toList());

        imageManipulationAdapter.teardown();

        return imageManipulationAdapter.getModifiedImages();
    }

}
