package com.arassec.artivact.domain.model.operation;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;

/**
 * Interface for background operations that can be executed asynchronously.
 */
public interface BackgroundOperation {

    /**
     * Executes the background operation.
     *
     * @param progressMonitor The progress monitor to track execution progress.
     */
    void execute(ProgressMonitor progressMonitor);

}
