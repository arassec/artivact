package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.service.BatchService;
import com.arassec.artivact.web.model.OperationProgress;
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

    /**
     * Service for batch processing.
     */
    private final BatchService batchService;

    /**
     * Batch processes items.
     *
     * @param parameters The parameters for batch processing.
     * @return An {@link OperationProgress}.
     */
    @PostMapping("/process")
    public ResponseEntity<OperationProgress> process(@RequestBody BatchProcessingParameters parameters) {
        batchService.process(parameters);
        return getProgress();
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(batchService.getProgressMonitor());
    }

}
