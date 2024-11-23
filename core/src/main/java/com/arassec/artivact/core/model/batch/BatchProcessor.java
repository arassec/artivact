package com.arassec.artivact.core.model.batch;

import com.arassec.artivact.core.model.item.Item;

/**
 * Defines a batch processor for items.
 */
public interface BatchProcessor {

    /**
     * Initializes the batch processor before each batch run.
     */
    void initialize();

    /**
     * Processes
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     * @return {@code true}, if the processed item needs saving, {@code false} otherwise.
     */
    boolean process(BatchProcessingParameters params, Item item);

}
