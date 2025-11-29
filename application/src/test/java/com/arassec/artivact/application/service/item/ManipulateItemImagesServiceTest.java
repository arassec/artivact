package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulatorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManipulateItemImagesServiceTest {

    private ManipulateItemImagesService service;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    @Mock
    private ImageManipulatorPeripheral imageManipulatorPeripheral;

    @BeforeEach
    void setUp() {
        List<Peripheral> peripherals = new ArrayList<>();
        peripherals.add(imageManipulatorPeripheral);

        service = new ManipulateItemImagesService(
                runBackgroundOperationUseCase,
                useProjectDirsUseCase,
                loadItemUseCase,
                saveItemUseCase,
                loadAdapterConfigurationUseCase,
                peripherals
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeBackgroundsProcessesImagesAndSavesResult() {
        String itemId = "item-123";
        String imageManipulatorConfigId = "bg-removal-1";
        int imageSetIndex = 0;
        Path imagesDir = Path.of("/project/items/item-123/images");
        Path projectRoot = Path.of("/project");

        CreationImageSet imageSet = CreationImageSet.builder()
                .files(new ArrayList<>(List.of("001.jpg", "002.jpg")))
                .build();

        Item item = new Item();
        item.setId(itemId);
        MediaCreationContent mediaCreationContent = new MediaCreationContent();
        mediaCreationContent.getImageSets().add(imageSet);
        item.setMediaCreationContent(mediaCreationContent);

        PeripheralConfig bgRemovalConfig = mock(PeripheralConfig.class);
        when(bgRemovalConfig.getId()).thenReturn(imageManipulatorConfigId);
        when(bgRemovalConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .imageBackgroundRemovalPeripheralConfigs(List.of(bgRemovalConfig))
                .build();

        when(loadItemUseCase.loadTranslated(itemId)).thenReturn(item);
        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(imageManipulatorPeripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL)).thenReturn(true);
        when(imageManipulatorPeripheral.getModifiedImages()).thenReturn(List.of(
                Path.of("001-nobg.png"),
                Path.of("002-nobg.png")
        ));

        doAnswer(invocation -> {
            Consumer consumer = invocation.getArgument(2);
            consumer.accept(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(eq("manipulateImage"), eq("backgroundRemovalStart"), any());

        service.removeBackgrounds(itemId, imageManipulatorConfigId, imageSetIndex);

        verify(imageManipulatorPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(imageManipulatorPeripheral).removeBackgrounds(any());
        verify(imageManipulatorPeripheral).teardown();
        verify(saveItemUseCase).save(item);
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeBackgroundsDoesNotSaveWhenNoImagesProduced() {
        String itemId = "item-123";
        String imageManipulatorConfigId = "bg-removal-1";
        int imageSetIndex = 0;
        Path imagesDir = Path.of("/project/items/item-123/images");
        Path projectRoot = Path.of("/project");

        CreationImageSet imageSet = CreationImageSet.builder()
                .files(new ArrayList<>(List.of("001.jpg")))
                .build();

        Item item = new Item();
        item.setId(itemId);
        MediaCreationContent mediaCreationContent = new MediaCreationContent();
        mediaCreationContent.getImageSets().add(imageSet);
        item.setMediaCreationContent(mediaCreationContent);

        PeripheralConfig bgRemovalConfig = mock(PeripheralConfig.class);
        when(bgRemovalConfig.getId()).thenReturn(imageManipulatorConfigId);
        when(bgRemovalConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .imageBackgroundRemovalPeripheralConfigs(List.of(bgRemovalConfig))
                .build();

        when(loadItemUseCase.loadTranslated(itemId)).thenReturn(item);
        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(imagesDir);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(imageManipulatorPeripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL)).thenReturn(true);
        when(imageManipulatorPeripheral.getModifiedImages()).thenReturn(List.of());

        doAnswer(invocation -> {
            Consumer consumer = invocation.getArgument(2);
            consumer.accept(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(eq("manipulateImage"), eq("backgroundRemovalStart"), any());

        service.removeBackgrounds(itemId, imageManipulatorConfigId, imageSetIndex);

        verify(imageManipulatorPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(imageManipulatorPeripheral).removeBackgrounds(any());
        verify(imageManipulatorPeripheral).teardown();
        verify(saveItemUseCase, never()).save(any());
    }
}
