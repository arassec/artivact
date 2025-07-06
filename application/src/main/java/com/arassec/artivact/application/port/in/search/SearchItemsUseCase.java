package com.arassec.artivact.application.port.in.search;

import com.arassec.artivact.domain.model.item.Item;

import java.util.List;

public interface SearchItemsUseCase {

    /**
     * Searches for items without restrictions and without translating results.
     *
     * @param query      The search query to use.
     * @param maxResults The maximum number of results.
     * @return The list of found items.
     */
    List<Item> search(String query, int maxResults);

    /**
     * Searches for items with the given query. Translates found items and restricts the result set
     * to the items available for the current user.
     *
     * @param query      The lucene search query.
     * @param maxResults Maximum number of results to return.
     * @return The list of found items.
     */
    List<Item> searchTranslatedRestricted(String query, int maxResults);

}
