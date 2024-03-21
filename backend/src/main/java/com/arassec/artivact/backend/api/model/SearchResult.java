package com.arassec.artivact.backend.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Models the result of an item search.
 */
@Builder
@Getter
@Setter
public class SearchResult {

    /**
     * The current page of the search result.
     */
    private int pageNumber;

    /**
     * The number of items on the page.
     */
    private int pageSize;

    /**
     * The total number of pages found by the search.
     */
    private long totalPages;

    /**
     * The found items of the current page.
     */
    @Builder.Default
    private List<ItemCardData> data = new LinkedList<>();

}
