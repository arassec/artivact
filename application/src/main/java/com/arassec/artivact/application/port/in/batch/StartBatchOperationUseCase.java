package com.arassec.artivact.application.port.in.batch;

import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;

/**
 * Use case for start batch operation operations.
 */
public interface StartBatchOperationUseCase {

    void process(BatchProcessingParameters parameters);

}
