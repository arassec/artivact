package com.arassec.artivact.vault.backend.service.model.search;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Builder
@Data
public class ArtivactSearchResult {

    private int pageNumber;

    private int pageSize;

    private long totalPages;

    @Builder.Default
    private List<ArtivactSearchData> data = new LinkedList<>();

}
