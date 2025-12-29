package com.arassec.artivact.application.port.out.gateway;

import com.arassec.artivact.domain.model.item.Item;

import java.util.List;

/**
 * Interface for search gateway.
 */
public interface SearchGateway {

    /**
     * Prepares indexing.
     *
     * @param append Set to {@code true} to append to the current search index rather than create a new one.
     */
    void prepareIndexing(boolean append);

    /**
     * Updates the search index for the given item.
     *
     * @param item        The item.
     * @param updateIndex Set to {@code true} to update an existing index instead of creating a new entry for the item.
     */
    void updateIndex(Item item, boolean updateIndex);

    /**
     * Finalize indexing.
     */
    void finalizeIndexing();

    /**
     * Searches for items with the given query.
     *
     * @param searchQuery The lucene search query.
     * @param maxResults  Maximum number of results to return.
     * @return The list of found item IDs.
     */
    List<String> search(String searchQuery, int maxResults);

}
