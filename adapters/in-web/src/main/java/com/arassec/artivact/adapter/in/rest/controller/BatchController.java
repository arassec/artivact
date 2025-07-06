package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.adapter.in.rest.model.OperationProgress;
import com.arassec.artivact.application.port.in.batch.StartBatchOperationUseCase;
import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller for batch processing.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/batch")
public class BatchController extends BaseController {

    private final StartBatchOperationUseCase startBatchOperationUseCase;

    private final GetBackgroundOperationProgressUseCase getBackgroundOperationProgressUseCase;

    /**
     * Batch processes items.
     *
     * @param parameters The parameters for batch processing.
     * @return An {@link OperationProgress}.
     */
    @PostMapping("/process")
    public ResponseEntity<OperationProgress> process(@RequestBody BatchProcessingParameters parameters) {
        startBatchOperationUseCase.process(parameters);
        return getProgress();
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(getBackgroundOperationProgressUseCase.getProgress());
    }

}
