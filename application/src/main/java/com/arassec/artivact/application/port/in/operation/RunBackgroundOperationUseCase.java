package com.arassec.artivact.application.port.in.operation;

import com.arassec.artivact.domain.model.operation.BackgroundOperation;

/**
 * Use case for run background operation operations.
 */
public interface RunBackgroundOperationUseCase {

    /**
     * Executes a background operation.
     *
     * @param topic               The topic of the operation.
     * @param step                The step of the operation.
     * @param backgroundOperation The background operation to execute.
     */
    void execute(String topic, String step, BackgroundOperation backgroundOperation);

}
