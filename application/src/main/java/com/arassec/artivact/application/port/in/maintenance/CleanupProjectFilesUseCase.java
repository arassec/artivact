package com.arassec.artivact.application.port.in.maintenance;

/**
 * Use case for cleanup project files operations.
 */
public interface CleanupProjectFilesUseCase {

    /**
     * Checks project files for consistency and cleans them up if necessary.
     */
    void cleanup();

}
