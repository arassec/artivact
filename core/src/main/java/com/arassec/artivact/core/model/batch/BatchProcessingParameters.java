package com.arassec.artivact.core.model.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines Parameters for batch processing of items.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchProcessingParameters {

    /**
     * The task to perform.
     */
    private BatchProcessingTask task;

    /**
     * Search term defining the items to manipulate with the configured task.
     */
    private String searchTerm;

    /**
     * Maximum number of items to process.
     */
    private int maxItems;

    /**
     * The ID of the object to apply the task to.
     */
    private String targetId;

}
