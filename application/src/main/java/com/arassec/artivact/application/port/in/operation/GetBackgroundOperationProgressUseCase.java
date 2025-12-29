package com.arassec.artivact.application.port.in.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;

/**
 * Use case for get background operation progress operations.
 */
public interface GetBackgroundOperationProgressUseCase {

    ProgressMonitor getProgress();

}
