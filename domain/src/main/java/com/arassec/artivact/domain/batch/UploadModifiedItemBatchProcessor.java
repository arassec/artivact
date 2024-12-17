package com.arassec.artivact.domain.batch;

import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.core.model.batch.BatchProcessingTask;
import com.arassec.artivact.core.model.batch.BatchProcessor;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.domain.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link BatchProcessor} to upload a modified item to a remote Artivact instance.
 */
@Component
@RequiredArgsConstructor
public class UploadModifiedItemBatchProcessor implements BatchProcessor {

    /**
     * Service for items.
     */
    private final ItemService itemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (BatchProcessingTask.UPLOAD_MODIFIED_ITEM.equals(params.getTask())) {
            itemService.uploadItemToRemoteInstance(item.getId(), false);
            item.setSyncVersion(item.getVersion() + 1); // Saving the item later increments the "version" property!
            return true;
        }
        return false;
    }

}
