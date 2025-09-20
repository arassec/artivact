package com.arassec.artivact.application.service.operation;

import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * Service implementation of use cases related to background operations.
 */
@Slf4j
@Service
public class BackgroundOperationService implements RunBackgroundOperationUseCase, GetBackgroundOperationProgressUseCase {

    /**
     * The service's progress monitor for long-running tasks.
     */
    private ProgressMonitor progressMonitor;

    /**
     * Executor service for background tasks.
     */
    private final ExecutorService executorService;

    /**
     * Creates a new instance.
     *
     * @param executorService The executor service to use for thread execution.
     */
    public BackgroundOperationService(@Qualifier("backgroundOperationExecutorService") ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Cleans up the service.
     */
    @PreDestroy
    public void teardown() {
        executorService.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String topic, String step, BackgroundOperation backgroundOperation) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            log.warn("Background operation has not been executed due to unfinished operation!");
            return;
        }

        progressMonitor = new ProgressMonitor(topic, step);

        executorService.submit(() -> {
            try {
                log.info("Starting background Operation: {}/{}", topic, step);
                backgroundOperation.execute(progressMonitor);
                log.info("Background operation finished!");
                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("failed", e);
                log.error("Error during background operation!", e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProgressMonitor getProgress() {
        return progressMonitor;
    }

}
