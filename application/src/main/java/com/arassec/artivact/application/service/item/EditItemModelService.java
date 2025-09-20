package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.EditItemModelUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditItemModelService implements EditItemModelUseCase {

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final LoadPeripheralConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available peripheral adapters.
     */
    private final List<Peripheral> peripheralAdapters;


    /**
     * Opens the item's model in an external 3D editor.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index to open the model from.
     */
    @Override
    public synchronized void editModel(String itemId, int modelSetIndex) {
        runBackgroundOperationUseCase.execute("editModel", "start", progressMonitor -> {
            Item item = loadItemUseCase.loadTranslated(itemId);
            CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
            editModel(progressMonitor, creationModelSet);
        });
    }

    /**
     * Opens the selected model in the currently configured model-editor.
     *
     * @param progressMonitor The progress monitor to show status updates to the user.
     * @param creationModel   The model to open.
     */
    public void editModel(ProgressMonitor progressMonitor, CreationModelSet creationModel) {
        PeripheralConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadPeripheralConfiguration();

        ModelEditorPeripheral modelEditorAdapter = peripheralAdapters.stream()
                .filter(ModelEditorPeripheral.class::isInstance)
                .map(ModelEditorPeripheral.class::cast)
                .filter(adapter -> adapter.supports(adapterConfiguration.getModelEditorPeripheralImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected model-editor adapter!"));

        modelEditorAdapter.initialize(progressMonitor, PeripheralInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .configuration(adapterConfiguration)
                .build());

        modelEditorAdapter.open(creationModel);

        modelEditorAdapter.teardown();
    }

}
