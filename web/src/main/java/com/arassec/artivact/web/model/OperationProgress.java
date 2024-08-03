package com.arassec.artivact.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Progress indicator for long-running background operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationProgress {

    /**
     * A label key used to translate the progress in the frontend.
     */
    private String key;

    /**
     * The current amount already processed.
     */
    private Integer currentAmount;

    /**
     * The target amount to be reached when finished.
     */
    private Integer targetAmount;

    /**
     * Possible error messages in case of failures during progress.
     */
    private String error;

}
