package com.arassec.artivact.backend.service.util;

import lombok.Getter;

/**
 * Simple container for progress to show to the user.
 */
public class ProgressMonitor {

    /**
     * The progress string to show.
     */
    @Getter
    private String progress = "Starting";

    /**
     * Creates a new instance.
     */
    public ProgressMonitor() {
    }

    /**
     * Updates the progress string.
     *
     * @param progress The progress to set.
     */
    public void updateProgress(String progress) {
        this.progress = progress;
    }

}
