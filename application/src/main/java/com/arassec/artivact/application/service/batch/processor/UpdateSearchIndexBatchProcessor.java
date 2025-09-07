package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Batch processor that updates the search index for all matching items.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateSearchIndexBatchProcessor implements BatchProcessor {

    /**
     * Use case for managing the search index.
     */
    private final ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * Re-creates the search index completely if the search matches all items.
     *
     * @param params The parameters for batch processing.
     * @return {@code true} if the search index has been re-created completely and further processing should stop.
     */
    @Override
    public boolean processAllExclusive(BatchProcessingParameters params) {
        if (!BatchProcessingTask.UPDATE_SEARCH_INDEX.equals(params.getTask())) {
            return false;
        }

        if ("*".equals(params.getSearchTerm())) {
            log.info("Re-creating the search index for all items.");
            manageSearchIndexUseCase.recreateIndex();
            return true;
        }

        return false;
    }

    /**
     * Updates the search index for all relevant items matching the search criteria.
     *
     * @param params The parameters for batch processing an item.
     * @param item   The item to process.
     * @return {@code true} if the search index has been updated, {@code false} otherwise.
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        if (!BatchProcessingTask.UPDATE_SEARCH_INDEX.equals(params.getTask())) {
            return false;
        }

        manageSearchIndexUseCase.updateIndex(item);

        return true;
    }

}
