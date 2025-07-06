package com.arassec.artivact.application.port.in.batch;

import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;

public interface StartBatchOperationUseCase {

    void process(BatchProcessingParameters parameters);

}
