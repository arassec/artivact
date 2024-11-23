package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.core.model.batch.BatchProcessor;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for batch processing.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BatchService {

    /**
     * Service for searching items.
     */
    private final SearchService searchService;

    /**
     * Service for item processing.
     */
    private final ItemService itemService;

    /**
     * List with all available batch processors.
     */
    private final List<BatchProcessor> batchProcessors;

    /**
     * The service's progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Executor service for background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Processes items in batch using the supplied parameters.
     *
     * @param parameters The parameters for the batch processing.
     */
    public void process(BatchProcessingParameters parameters) {
        if (!StringUtils.hasText(parameters.getSearchTerm())) {
            return;
        }

        if (parameters.getTask() == null) {
            return;
        }

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "process");

        executorService.submit(() -> {
            try {
                log.info("Starting batch processing of items: {}", parameters.getTask());
                batchProcessors.forEach(BatchProcessor::initialize);

                searchService.search(parameters.getSearchTerm(), Integer.MAX_VALUE).forEach(
                        item -> batchProcessors.stream()
                                .map(batchProcessor -> batchProcessor.process(parameters, item))
                                .filter(result -> result)
                                .findFirst()
                                .ifPresent(result -> itemService.save(item))
                );
                progressMonitor = null;
                log.info("Batch processing finished!");
            } catch (Exception e) {
                progressMonitor.updateProgress("processingFailed", e);
                log.error("Error during batch processing!", e);
            }
        });
    }

}
