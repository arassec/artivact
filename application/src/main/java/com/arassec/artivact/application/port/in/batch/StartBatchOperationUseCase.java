package com.arassec.artivact.application.port.in.batch;

import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;

/**
 * Use case for start batch operation operations.
 */
public interface StartBatchOperationUseCase {

    /**
     * Processes a batch operation with the given parameters.
     *
     * @param parameters The batch processing parameters.
     */
    void process(BatchProcessingParameters parameters);

}
