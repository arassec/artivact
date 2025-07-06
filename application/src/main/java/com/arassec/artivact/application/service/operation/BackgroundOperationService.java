package com.arassec.artivact.application.service.operation;

import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Cleans up the service.
     */
    @PreDestroy
    public void teardown() {
        executorService.shutdownNow();
    }

    @Override
    public void execute(Class<?> clazz, String labelKeySuffix, BackgroundOperation backgroundOperation) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            log.warn("Background operation has not been executed due to unfinished operation!");
            return;
        }

        progressMonitor = new ProgressMonitor(clazz, labelKeySuffix);

        executorService.submit(() -> {
            try {
                log.info("Starting background Operation: {}", backgroundOperation);
                backgroundOperation.execute(progressMonitor);
                log.info("Background operation finished!");
                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("processingFailed", e);
                log.error("Error during background operation!", e);
            }
        });
    }

    @Override
    public ProgressMonitor getProgress() {
        return progressMonitor;
    }

}
