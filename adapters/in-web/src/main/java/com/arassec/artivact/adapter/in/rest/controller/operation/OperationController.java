package com.arassec.artivact.adapter.in.rest.controller.operation;

import com.arassec.artivact.adapter.in.rest.model.OperationProgress;
import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for management of long-running background operations.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/operation")
public class OperationController {

    /**
     * Use case to get the progress of a long-running background operation.
     */
    private final GetBackgroundOperationProgressUseCase getBackgroundOperationProgressUseCase;

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(getBackgroundOperationProgressUseCase.getProgress());
    }

    /**
     * Converts a {@link ProgressMonitor} into a {@link OperationProgress}.
     *
     * @param progressMonitor The progress monitor to convert.
     * @return An operation progress containing the monitor's data.
     */
    private ResponseEntity<OperationProgress> convert(ProgressMonitor progressMonitor) {
        if (progressMonitor != null) {
            OperationProgress operationProgress = new OperationProgress();
            operationProgress.setKey(progressMonitor.getLabelKey());
            operationProgress.setCurrentAmount(progressMonitor.getCurrentAmount());
            operationProgress.setTargetAmount(progressMonitor.getTargetAmount());
            if (progressMonitor.getException() != null) {
                operationProgress.setError(ExceptionUtils.getStackTrace(progressMonitor.getException()));
            }
            return ResponseEntity.ok(operationProgress);
        }
        return ResponseEntity.ok().build();
    }

}
