package com.arassec.artivact.core.model.batch;

/**
 * Tasks that can be performed with item batch processing.
 */
public enum BatchProcessingTask {

    /**
     * Deletes an item.
     */
    DELETE_ITEM,

    /**
     * Adds a tag to an item.
     */
    ADD_TAG_TO_ITEM,

    /**
     * Removes a tag from an item.
     */
    REMOVE_TAG_FROM_ITEM,

    /**
     * Uploads all modified items to a remote Artivact instance.
     */
    UPLOAD_MODIFIED_ITEM

}
