package com.arassec.artivact.backend.service.util;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Simple container for progress to show to the user.
 */
@Data
public class ProgressMonitor {

    /**
     * Prefix which is fix for the progress feedback.
     */
    private String progressPrefix;

    /**
     * The progress string to show.
     */
    private String progress = "Starting";

    /**
     * Indicates whether progress was cancelled by the user or not.
     */
    private boolean cancelled;

    /**
     * Creates a new instance.
     */
    public ProgressMonitor() {
    }

    /**
     * Creates a new instance.
     *
     * @param progressPrefix The prefix to use.
     */
    public ProgressMonitor(String progressPrefix) {
        this.progressPrefix = progressPrefix;
    }

    /**
     * Updates the progress string.
     *
     * @param progress The progress to set.
     */
    public void updateProgress(String progress) {
        if (StringUtils.hasText(progressPrefix)) {
            this.progress = progressPrefix + " " + progress;
        } else {
            this.progress = progress;
        }
    }

    /**
     * Sets the progress prefix.
     *
     * @param progressPrefix The progress prefix to set.
     */
    public void setProgressPrefix(String progressPrefix) {
        this.progressPrefix = progressPrefix;
        this.progress = progressPrefix;
    }

}
