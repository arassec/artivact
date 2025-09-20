package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ImageManipulationPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManipulateItemImagesServiceTest {

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private LoadPeripheralConfigurationUseCase loadPeripheralConfigurationUseCase;

    @Mock
    private ImageManipulationPeripheral imageManipulationPeripheral;

    @InjectMocks
    private ManipulateItemImagesService service;

    @Test
    void testRemoveBackgroundsAddsNewImageSetWhenImagesProcessed() {
        String itemId = "item1";
        CreationImageSet creationImageSet = CreationImageSet.builder()
                .files(List.of("img1.png"))
                .build();

        Item item = new Item();
        item.getMediaCreationContent().getImageSets().add(creationImageSet);

        PeripheralConfiguration config = new PeripheralConfiguration();
        config.setImageManipulationPeripheralImplementation(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);

        when(loadItemUseCase.loadTranslated(itemId)).thenReturn(item);
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(config);
        when(imageManipulationPeripheral.supports(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL)).thenReturn(true);
        when(imageManipulationPeripheral.getModifiedImages()).thenReturn(List.of(Path.of("img1-bg-removed.png")));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("/root"));
        when(useProjectDirsUseCase.getImagesDir(itemId)).thenReturn(Path.of("/root/items/item1/images"));

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service = new ManipulateItemImagesService(
                runBackgroundOperationUseCase,
                useProjectDirsUseCase,
                loadItemUseCase,
                saveItemUseCase,
                loadPeripheralConfigurationUseCase,
                List.of(imageManipulationPeripheral));

        service.removeBackgrounds(itemId, 0);

        verify(saveItemUseCase).save(argThat(saved ->
                saved.getMediaCreationContent().getImageSets().size() == 2 &&
                        saved.getMediaCreationContent().getImageSets().get(1).getBackgroundRemoved()
        ));
        verify(imageManipulationPeripheral).initialize(any(), any());
        verify(imageManipulationPeripheral).removeBackgrounds(anyList());
        verify(imageManipulationPeripheral).teardown();
    }

    @Test
    void testRemoveBackgroundsThrowsWhenNoAdapterFound() {
        String itemId = "itemX";
        CreationImageSet set = CreationImageSet.builder().files(List.of("f.png")).build();
        Item item = new Item();
        item.getMediaCreationContent().getImageSets().add(set);

        PeripheralConfiguration config = new PeripheralConfiguration();
        config.setImageManipulationPeripheralImplementation(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);

        when(loadItemUseCase.loadTranslated(itemId)).thenReturn(item);
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(config);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service = new ManipulateItemImagesService(
                runBackgroundOperationUseCase,
                useProjectDirsUseCase,
                loadItemUseCase,
                saveItemUseCase,
                loadPeripheralConfigurationUseCase,
                List.of() // no valid adapter!
        );

        assertThatThrownBy(() -> service.removeBackgrounds(itemId, 0))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("image-manipulation adapter");
    }

}
