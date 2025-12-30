package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.EditItemModelUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Service for edit item model.
 */
public class EditItemModelService implements EditItemModelUseCase {

    /**
     * Use case for run background operation.
     */
    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for load item.
     */
    private final LoadItemUseCase loadItemUseCase;

    /**
     * Use case for load adapter configuration.
     */
    private final LoadPeripheralsConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available peripheral adapters.
     */
    private final List<Peripheral> peripheralAdapters;


    /**
     * Opens the item's model in an external 3D editor.
     *
     * @param itemId              The item's ID.
     * @param modelEditorConfigId The ID of the model editor configuration to use.
     * @param modelSetIndex       The model-set index to open the model from.
     */
    @Override
    public synchronized void editModel(String itemId, String modelEditorConfigId, int modelSetIndex) {
        runBackgroundOperationUseCase.execute("editModel", "start", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);
            CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
            editModel(modelEditorConfigId, progressMonitor, creationModelSet);
        });
    }

    /**
     * Opens the selected model in the currently configured model-editor.
     *
     * @param modelEditorConfigId The ID of the model editor configuration to use.
     * @param progressMonitor     The progress monitor to show status updates to the user.
     * @param creationModel       The model to open.
     */
    public void editModel(String modelEditorConfigId, ProgressMonitor progressMonitor, CreationModelSet creationModel) {
        PeripheralsConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        PeripheralConfig modelEditorConfig = adapterConfiguration.getModelEditorPeripheralConfigs().stream()
                .filter(config -> config.getId().equals(modelEditorConfigId))
                .findFirst()
                .orElseThrow();

        ModelEditorPeripheral modelEditorAdapter = peripheralAdapters.stream()
                .filter(ModelEditorPeripheral.class::isInstance)
                .map(ModelEditorPeripheral.class::cast)
                .filter(adapter -> adapter.supports(modelEditorConfig.getPeripheralImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected model-editor adapter!"));

        modelEditorAdapter.initialize(progressMonitor, PeripheralInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .config(modelEditorConfig)
                .build());

        modelEditorAdapter.open(creationModel);

        modelEditorAdapter.teardown();
    }

}
