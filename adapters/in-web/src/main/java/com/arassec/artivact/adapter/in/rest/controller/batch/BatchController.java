package com.arassec.artivact.adapter.in.rest.controller.batch;

import com.arassec.artivact.adapter.in.rest.controller.BaseController;
import com.arassec.artivact.application.port.in.batch.StartBatchOperationUseCase;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller for batch processing.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/batch")
public class BatchController extends BaseController {

    /**
     * Use case to start a batch operation.
     */
    private final StartBatchOperationUseCase startBatchOperationUseCase;

    /**
     * Batch processes items.
     *
     * @param parameters The parameters for batch processing.
     */
    @PostMapping("/process")
    public void process(@RequestBody BatchProcessingParameters parameters) {
        startBatchOperationUseCase.process(parameters);
    }

}
