package com.arassec.artivact.application.port.in.operation;

import com.arassec.artivact.domain.model.operation.BackgroundOperation;

public interface RunBackgroundOperationUseCase {

    void execute(String topic, String step, BackgroundOperation backgroundOperation);

}
