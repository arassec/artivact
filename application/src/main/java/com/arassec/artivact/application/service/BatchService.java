package com.arassec.artivact.application.service;

import com.arassec.artivact.application.port.in.batch.StartBatchOperationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service for batch processing.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BatchService implements StartBatchOperationUseCase {

    /**
     * Service for searching items.
     */
    private final SearchItemsUseCase searchItemsUseCase;

    private final LoadItemUseCase loadItemUseCase;

    private final SaveItemUseCase saveItemUseCase;

    /**
     * List with all available batch processors.
     */
    private final List<BatchProcessor> batchProcessors;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Processes items in batch using the supplied parameters.
     *
     * @param parameters The parameters for the batch processing.
     */
    @Override
    public void process(BatchProcessingParameters parameters) {
        if (!StringUtils.hasText(parameters.getSearchTerm())
                && !BatchProcessingTask.UPLOAD_MODIFIED_ITEM.equals(parameters.getTask())) {
            return;
        }

        if (parameters.getTask() == null || parameters.getMaxItems() <= 0) {
            return;
        }

        runBackgroundOperationUseCase.execute(getClass(), "process", progressMonitor -> {
            log.info("Starting batch processing of items: {}", parameters.getTask());
            batchProcessors.forEach(BatchProcessor::initialize);

            List<Item> itemsToProcess;
            if (BatchProcessingTask.UPLOAD_MODIFIED_ITEM.equals(parameters.getTask())) {
                itemsToProcess = loadItemUseCase.loadModified(parameters.getMaxItems());
            } else {
                itemsToProcess = searchItemsUseCase.search(parameters.getSearchTerm(), parameters.getMaxItems());
            }

            itemsToProcess.forEach(
                    item -> batchProcessors.stream()
                            .map(batchProcessor -> batchProcessor.process(parameters, item))
                            .filter(result -> result)
                            .findFirst()
                            .ifPresent(result -> saveItemUseCase.save(item))
            );
            log.info("Batch processing finished!");
        });
    }

}
