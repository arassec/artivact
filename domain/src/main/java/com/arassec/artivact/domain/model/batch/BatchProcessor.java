package com.arassec.artivact.domain.model.batch;

import com.arassec.artivact.domain.model.item.Item;

/**
 * Defines a batch processor for items.
 */
public interface BatchProcessor {

    /**
     * Initializes the batch processor before each batch run.
     */
    default void initialize() {
    }

    /**
     * Processes all items.
     *
     * @param params The parameters for batch processing.
     * @return {@code true} if all items have been processed, and no further processing should be done.
     */
    default boolean processAllExclusive(BatchProcessingParameters params) {
        return false;
    }

    /**
     * Processes the single item by the batch processor.
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     * @return {@code true}, if the processed item needs saving, {@code false} otherwise.
     */
    default boolean process(BatchProcessingParameters params, Item item) {
        return false;
    }

}
