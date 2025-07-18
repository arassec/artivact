package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.item.DeleteItemUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link BatchProcessor} that deletes items.
 */
@Component
@RequiredArgsConstructor
public class DeleteItemBatchProcessor implements BatchProcessor {

    /**
     * Service for items.
     */
    private final DeleteItemUseCase deleteItemUseCase;

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
            deleteItemUseCase.delete(item.getId());
        }
        return false;
    }

}
