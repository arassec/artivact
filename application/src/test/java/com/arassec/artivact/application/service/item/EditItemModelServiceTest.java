package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditItemModelServiceTest {

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private LoadPeripheralConfigurationUseCase loadPeripheralConfigurationUseCase;

    @Mock
    private ModelEditorPeripheral modelEditorPeripheral;

    @Mock
    private PeripheralConfiguration peripheralConfiguration;

    @Mock
    private Item item;

    @Mock
    private CreationModelSet creationModelSet;

    @Mock
    private ProgressMonitor progressMonitor;

    @InjectMocks
    private EditItemModelService editItemModelService;

    @BeforeEach
    void setUp() {
        editItemModelService = new EditItemModelService(
                runBackgroundOperationUseCase,
                useProjectDirsUseCase,
                loadItemUseCase,
                loadPeripheralConfigurationUseCase,
                List.of(modelEditorPeripheral)
        );
    }

    @Test
    void testEditModelWithItemIdAndIndexSuccessfulFlow() {
        // Arrange
        when(loadItemUseCase.loadTranslated("item-1")).thenReturn(item);
        MediaCreationContent mediaCreationContent = new MediaCreationContent();
        mediaCreationContent.setModelSets(List.of(creationModelSet));
        when(item.getMediaCreationContent()).thenReturn(mediaCreationContent);
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralConfiguration);
        when(peripheralConfiguration.getModelEditorPeripheralImplementation()).thenReturn(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL);
        when(modelEditorPeripheral.supports(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL)).thenReturn(true);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("/project"));

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(progressMonitor);
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        // Act
        editItemModelService.editModel("item-1", 0);

        // Assert
        verify(modelEditorPeripheral).initialize(eq(progressMonitor), any(PeripheralInitParams.class));
        verify(modelEditorPeripheral).open(creationModelSet);
        verify(modelEditorPeripheral).teardown();
    }

    @Test
    void testEditModelWithProgressMonitorSuccessfulFlow() {
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralConfiguration);
        when(peripheralConfiguration.getModelEditorPeripheralImplementation()).thenReturn(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL);
        when(modelEditorPeripheral.supports(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL)).thenReturn(true);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("/root"));

        editItemModelService.editModel(progressMonitor, creationModelSet);

        verify(modelEditorPeripheral).initialize(eq(progressMonitor), any(PeripheralInitParams.class));
        verify(modelEditorPeripheral).open(creationModelSet);
        verify(modelEditorPeripheral).teardown();
    }

    @Test
    void testEditModelWithProgressMonitorNoMatchingAdapterThrows() {
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralConfiguration);
        when(peripheralConfiguration.getModelEditorPeripheralImplementation()).thenReturn(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL);
        when(modelEditorPeripheral.supports(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL)).thenReturn(false);

        assertThatThrownBy(() -> editItemModelService.editModel(progressMonitor, creationModelSet))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not detect selected model-editor adapter!");
    }

}
