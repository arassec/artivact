package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.LoadAdapterConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.EditItemModelUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.adapter.ModelEditorAdapter;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;
import com.arassec.artivact.domain.model.adapter.PeripheralAdapterInitParams;
import com.arassec.artivact.domain.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
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

    private final LoadAdapterConfigurationUseCase loadAdapterConfigurationUseCase;

    /**
     * List of all available peripheral adapters.
     */
    private final List<PeripheralAdapter> peripheralAdapters;


    /**
     * Opens the item's model in an external 3D editor.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index to open the model from.
     */
    @Override
    public synchronized void editModel(String itemId, int modelSetIndex) {
        runBackgroundOperationUseCase.execute(getClass(), "editModelStart", progressMonitor -> {
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
    private void editModel(ProgressMonitor progressMonitor, CreationModelSet creationModel) {
        AdapterConfiguration adapterConfiguration = loadAdapterConfigurationUseCase.loadAdapterConfiguration();

        ModelEditorAdapter modelEditorAdapter = peripheralAdapters.stream()
                .filter(ModelEditorAdapter.class::isInstance)
                .map(ModelEditorAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterConfiguration.getModelEditorImplementation()))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected model-editor adapter!"));

        modelEditorAdapter.initialize(progressMonitor, PeripheralAdapterInitParams.builder()
                .projectRoot(useProjectDirsUseCase.getProjectRoot())
                .adapterConfiguration(adapterConfiguration)
                .build());

        modelEditorAdapter.open(creationModel);

        modelEditorAdapter.teardown();
    }

}
