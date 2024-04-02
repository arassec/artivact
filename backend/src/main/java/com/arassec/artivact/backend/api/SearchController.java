package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.ItemCardData;
import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.api.model.SearchResult;
import com.arassec.artivact.backend.service.SearchService;
import com.arassec.artivact.backend.service.model.item.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller for searching items and search engine management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController extends BaseController {

    /**
     * The application's {@link SearchService}.
     */
    private final SearchService searchService;

    /**
     * Re-creates the search index completely.
     *
     * @return The progress.
     */
    @PostMapping("/index/recreate")
    public ResponseEntity<OperationProgress> recreateIndex() {
        searchService.recreateIndex();
        return getProgress();
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(searchService.getProgressMonitor());
    }

    /**
     * Searches for items.
     *
     * @param searchTerm The search query to use.
     * @param pageNumber The desired page to return from the search result.
     * @param pageSize   The desired page size of the search result.
     * @param maxResults The max number of results to consider.
     * @return The search result.
     */
    @GetMapping
    public SearchResult search(@RequestParam("query") String searchTerm,
                               @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "9") int pageSize,
                               @RequestParam(value = "maxResults", required = false, defaultValue = "100") int maxResults) {
        List<Item> items = searchService.searchTranslatedRestricted(searchTerm, maxResults);
        return getSearchResult(items, pageNumber, pageSize);
    }

    /**
     * Converts the given item list into a {@link SearchResult}.
     *
     * @param all        All found items.
     * @param pageNumber The page number to return.
     * @param pageSize   The page size to use.
     * @return The search result.
     */
    private SearchResult getSearchResult(List<Item> all, int pageNumber, int pageSize) {
        if (all != null && !all.isEmpty() && pageSize > 0) {
            long totalPages = all.size() / pageSize;
            if (all.size() % pageSize > 0) {
                totalPages++;
            }
            if (totalPages > 0 && totalPages > pageNumber) {
                int startIndex = pageNumber * pageSize;
                int endIndex = startIndex + pageSize;
                if (endIndex >= all.size()) {
                    endIndex = all.size();
                }
                return SearchResult.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalPages(totalPages)
                        .data(all.stream()
                                .map(item -> ItemCardData.builder()
                                        .itemId(item.getId())
                                        .title(item.getTitle())
                                        .imageUrl(createMainImageUrl(item))
                                        .hasModel(!item.getMediaContent().getModels().isEmpty())
                                        .build()
                                ).toList()
                                .subList(startIndex, endIndex))
                        .build();
            }
        }
        return SearchResult.builder()
                .pageNumber(pageNumber)
                .pageSize(0)
                .totalPages(0)
                .data(List.of())
                .build();
    }

}
