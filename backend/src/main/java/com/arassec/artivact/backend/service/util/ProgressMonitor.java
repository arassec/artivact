package com.arassec.artivact.backend.service.util;

import lombok.Getter;

/**
 * Simple container for progress to show to the user.
 */
@Getter
public class ProgressMonitor {

    /**
     * The progress string to show.
     */
    private String progress = "Starting";

    /**
     * An exception that occurred during processing.
     */
    private Exception exception;

    /**
     * Creates a new instance.
     */
    public ProgressMonitor() {
    }

    /**
     * Creates a new instance.
     *
     * @param progress The initial progress to use.
     */
    public ProgressMonitor(String progress) {
        this.progress = progress;
    }

    /**
     * Updates the progress string.
     *
     * @param progress The progress to set.
     */
    public void updateProgress(String progress) {
        this.progress = progress;
    }

    /**
     * Updates the progress string.
     *
     * @param progress The progress to set.
     * @param exception An exception that occurred during processing.
     */
    public void updateProgress(String progress, Exception exception) {
        this.progress = progress;
        this.exception = exception;
    }

}
