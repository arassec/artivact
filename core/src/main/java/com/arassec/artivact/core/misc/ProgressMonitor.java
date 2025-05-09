package com.arassec.artivact.core.misc;

import lombok.Getter;

/**
 * Simple container for progress to show to the user.
 */
public class ProgressMonitor {

    /**
     * Prefix for all label keys.
     */
    private static final String LABEL_KEY_PREFIX = "Progress";

    /**
     * The class providing this monitor instance.
     */
    private final Class<?> clazz;

    /**
     * The progress label to show.
     */
    @Getter
    private String labelKey;

    /**
     * The target amount to progress to.
     */
    @Getter
    private Integer targetAmount = 0;

    /**
     * The amount currently reached.
     */
    @Getter
    private Integer currentAmount = 0;

    /**
     * An exception that occurred during processing.
     */
    @Getter
    private Exception exception;

    /**
     * Creates a new instance.
     *
     * @param clazz          The class maintaining this monitor.
     * @param labelKeySuffix The key suffix for the label to display.
     */
    public ProgressMonitor(Class<?> clazz, String labelKeySuffix) {
        this.clazz = clazz;
        updateLabelKey(labelKeySuffix);
    }

    /**
     * Updates the current and target amount of progress.
     *
     * @param currentAmount The current amount of progress.
     * @param targetAmount  The target amount.
     */
    public void updateProgress(Integer currentAmount, Integer targetAmount) {
        this.currentAmount = currentAmount;
        this.targetAmount = targetAmount;
    }

    /**
     * Updates the current amount of progress.
     *
     * @param currentAmount The current amount of progress.
     */
    public void updateProgress(Integer currentAmount) {
        this.currentAmount = currentAmount;
    }

    /**
     * Updates the progress string.
     *
     * @param labelKeySuffix The key suffix for the label to display.
     * @param exception      An exception that occurred during processing.
     */
    public void updateProgress(String labelKeySuffix, Exception exception) {
        updateLabelKey(labelKeySuffix);
        this.exception = exception;
    }

    /**
     * Updates the label key.
     *
     * @param labelKeySuffix The key suffix for the label to display.
     */
    public void updateLabelKey(String labelKeySuffix) {
        this.labelKey = LABEL_KEY_PREFIX + "." + clazz.getSimpleName() + "." + labelKeySuffix;
    }

}
