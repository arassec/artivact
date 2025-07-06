package com.arassec.artivact.domain.model.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;

public interface BackgroundOperation {

    void execute(ProgressMonitor progressMonitor);

}
