package com.arassec.artivact.application.service.search;

import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.out.adapter.SearchAdapter;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Service for search-engine management and search handling.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService
        implements SearchItemsUseCase,
        ManageSearchIndexUseCase {

    /**
     * Repository for items.
     */
    private final ItemRepository itemRepository;

    private final SearchAdapter searchAdapter;

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Recreates the search index.
     */
    @Override
    public synchronized void recreateIndex() {
        runBackgroundOperationUseCase.execute(getClass(), "createIndex", progressMonitor -> {
            log.info("Recreating search index.");
            searchAdapter.prepareIndexing(false);
            itemRepository.findAll().forEach(item -> searchAdapter.updateIndex(item, false));
            searchAdapter.finalizeIndexing();
            log.info("Search index created.");
        });
    }

    /**
     * Updates an item's search index.
     *
     * @param item The item.
     */
    @Override
    public synchronized void updateIndex(Item item) {
        searchAdapter.prepareIndexing(true);
        searchAdapter.updateIndex(item, true);
        searchAdapter.finalizeIndexing();
    }

    /**
     * Searches for items without restrictions and without translating results.
     *
     * @param query      The search query to use.
     * @param maxResults The maximum number of results.
     * @return The list of found items.
     */
    @Override
    public List<Item> search(String query, int maxResults) {
        if (!StringUtils.hasText(query)) {
            return new LinkedList<>();
        }

        if ("*".equals(query)) {
            return itemRepository.findAll(maxResults);
        }

        return itemRepository.findAllById(searchAdapter.search(query, maxResults));
    }

    /**
     * Searches for items with the given query.
     *
     * @param query      The lucene search query.
     * @param maxResults Maximum number of results to return.
     * @return The list of found items.
     */
    @TranslateResult
    @RestrictResult
    @Override
    public List<Item> searchTranslatedRestricted(String query, int maxResults) {
        return search(query, maxResults);
    }

}
