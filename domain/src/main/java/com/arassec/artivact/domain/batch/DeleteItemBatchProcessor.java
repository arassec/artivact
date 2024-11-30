package com.arassec.artivact.domain.batch;

import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.core.model.batch.BatchProcessingTask;
import com.arassec.artivact.core.model.batch.BatchProcessor;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.domain.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link BatchProcessor} that deletes items.
 */
@Component
@RequiredArgsConstructor
public class DeleteItemBatchProcessor implements BatchProcessor {

    /**
     * Repository for items.
     */
    private final ItemService itemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here...
    }

    /**
     * Deletes the given item.
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (BatchProcessingTask.DELETE_ITEM.equals(params.getTask())) {
            itemService.delete(item.getId());
        }
        return false;
    }

}
