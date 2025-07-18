package com.arassec.artivact.domain.model.misc;

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
     * The topic of the operation.
     */
    private final String topic;

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
     * @param topic The topic of the operation.
     * @param step  The current step of the operation.
     */
    public ProgressMonitor(String topic, String step) {
        this.topic = topic;
        updateLabelKey(step);
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
     * @param step      The current step of the operation.
     * @param exception An exception that occurred during processing.
     */
    public void updateProgress(String step, Exception exception) {
        updateLabelKey(step);
        this.exception = exception;
    }

    /**
     * Updates the label key.
     *
     * @param step The current step of the operation.
     */
    public void updateLabelKey(String step) {
        this.labelKey = LABEL_KEY_PREFIX + "." + topic + "." + step;
    }

}
