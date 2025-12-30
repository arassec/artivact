package com.arassec.artivact.application.port.in.search;

import com.arassec.artivact.domain.model.item.Item;

/**
 * Use case for manage search index operations.
 */
public interface ManageSearchIndexUseCase {

    /**
     * Recreates the search index.
     */
    void recreateIndex();

    /**
     * Updates an item's search index.
     *
     * @param item The item.
     */
    void updateIndex(Item item);

}
