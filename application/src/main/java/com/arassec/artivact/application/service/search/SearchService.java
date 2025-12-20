package com.arassec.artivact.application.service.search;

import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.application.port.out.gateway.SearchGateway;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.item.Item;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Gateway to the search engine.
     */
    private final SearchGateway searchGateway;

    /**
     * The object mapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * Recreates the search index.
     */
    @Override
    public synchronized void recreateIndex() {
        log.info("Recreating search index.");
        searchGateway.prepareIndexing(false);
        itemRepository.findAll().forEach(item -> searchGateway.updateIndex(item, false));
        searchGateway.finalizeIndexing();
        log.info("Search index created.");
    }

    /**
     * Updates an item's search index.
     *
     * @param item The item.
     */
    @Override
    public synchronized void updateIndex(Item item) {
        searchGateway.prepareIndexing(true);
        searchGateway.updateIndex(item, true);
        searchGateway.finalizeIndexing();
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
            return itemRepository.findAll(maxResults).stream()
                    .sorted(Comparator.comparing(
                            o -> Optional.ofNullable(o.getTitle()).map(TranslatableString::getValue).orElse(null),
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .collect(Collectors.toList()); // Needs to be modifiable!
        }

        return itemRepository.findAllById(searchGateway.search(query, maxResults)).stream()
                .sorted(Comparator.comparing(
                        o -> Optional.ofNullable(o.getTitle()).map(TranslatableString::getValue).orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList()); // Needs to be modifiable!
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
