package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationModelSet;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditItemModelServiceTest {

    private EditItemModelService service;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    @Mock
    private ModelEditorPeripheral modelEditorPeripheral;

    @BeforeEach
    void setUp() {
        List<Peripheral> peripherals = new ArrayList<>();
        peripherals.add(modelEditorPeripheral);

        service = new EditItemModelService(
                runBackgroundOperationUseCase,
                useProjectDirsUseCase,
                loadItemUseCase,
                loadAdapterConfigurationUseCase,
                peripherals
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void editModelOpensModelInEditor() {
        String itemId = "item-123";
        String modelEditorConfigId = "editor-1";
        int modelSetIndex = 0;
        Path projectRoot = Path.of("/project");

        Item item = new Item();
        item.setId(itemId);
        MediaCreationContent mediaCreationContent = new MediaCreationContent();
        CreationModelSet modelSet = CreationModelSet.builder()
                .directory("items/item-123/models/001")
                .build();
        mediaCreationContent.getModelSets().add(modelSet);
        item.setMediaCreationContent(mediaCreationContent);

        PeripheralConfig editorConfig = mock(PeripheralConfig.class);
        when(editorConfig.getId()).thenReturn(modelEditorConfigId);
        when(editorConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .modelEditorPeripheralConfigs(List.of(editorConfig))
                .build();

        when(loadItemUseCase.loadTranslated(itemId)).thenReturn(item);
        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(modelEditorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL)).thenReturn(true);

        doAnswer(invocation -> {
            Consumer consumer = invocation.getArgument(2);
            consumer.accept(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(eq("editModel"), eq("start"), any());

        service.editModel(itemId, modelEditorConfigId, modelSetIndex);

        verify(modelEditorPeripheral).initialize(any(ProgressMonitor.class), any(PeripheralInitParams.class));
        verify(modelEditorPeripheral).open(modelSet);
        verify(modelEditorPeripheral).teardown();
    }

    @Test
    void editModelThrowsExceptionWhenNoMatchingPeripheral() {
        String modelEditorConfigId = "editor-1";
        Path projectRoot = Path.of("/project");

        PeripheralConfig editorConfig = mock(PeripheralConfig.class);
        when(editorConfig.getId()).thenReturn(modelEditorConfigId);
        when(editorConfig.getPeripheralImplementation()).thenReturn(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .modelEditorPeripheralConfigs(List.of(editorConfig))
                .build();

        when(loadAdapterConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(peripheralsConfiguration);
        when(modelEditorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL)).thenReturn(false);

        CreationModelSet modelSet = CreationModelSet.builder().build();
        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");

        assertThatThrownBy(() -> service.editModel(modelEditorConfigId, progressMonitor, modelSet))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not detect selected model-editor adapter!");
    }
}
