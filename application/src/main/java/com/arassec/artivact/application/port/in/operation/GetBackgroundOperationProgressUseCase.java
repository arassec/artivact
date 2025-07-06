package com.arassec.artivact.application.port.in.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;

public interface GetBackgroundOperationProgressUseCase {

    ProgressMonitor getProgress();

}
