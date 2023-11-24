package com.arassec.artivact.backend.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Builder
@Getter
@Setter
public class SearchResult {

    private int pageNumber;

    private int pageSize;

    private long totalPages;

    @Builder.Default
    private List<ItemCardData> data = new LinkedList<>();

}
