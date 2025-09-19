package com.arassec.artivact.application.service.search;

import com.arassec.artivact.application.port.out.gateway.SearchGateway;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    private ItemRepository itemRepository;
    private SearchGateway searchGateway;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        searchGateway = mock(SearchGateway.class);
        ObjectMapper objectMapper = new ObjectMapper();
        searchService = new SearchService(itemRepository, searchGateway, objectMapper);
    }

    @Test
    void testRecreateIndex() {
        Item item1 = new Item();
        Item item2 = new Item();
        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        searchService.recreateIndex();

        verify(searchGateway).prepareIndexing(false);
        verify(searchGateway).updateIndex(item1, false);
        verify(searchGateway).updateIndex(item2, false);
        verify(searchGateway).finalizeIndexing();
    }

    @Test
    void testUpdateIndex() {
        Item item = new Item();

        searchService.updateIndex(item);

        verify(searchGateway).prepareIndexing(true);
        verify(searchGateway).updateIndex(item, true);
        verify(searchGateway).finalizeIndexing();
    }

    @Test
    void testSearchWithEmptyQuery() {
        List<Item> result = searchService.search("", 10);

        assertThat(result).isEmpty();
        verifyNoInteractions(searchGateway);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void testSearchWithWildcardQuery() {
        Item item = new Item();
        when(itemRepository.findAll(5)).thenReturn(List.of(item));

        List<Item> result = searchService.search("*", 5);

        assertThat(result).containsExactly(item);
        verify(itemRepository).findAll(5);
        verifyNoInteractions(searchGateway);
    }

    @Test
    void testSearchWithNormalQuery() {
        when(searchGateway.search("abc", 3)).thenReturn(List.of("id1", "id2"));
        Item item1 = new Item();
        Item item2 = new Item();
        when(itemRepository.findAllById(List.of("id1", "id2"))).thenReturn(List.of(item1, item2));

        List<Item> result = searchService.search("abc", 3);

        assertThat(result).containsExactly(item1, item2);
        verify(searchGateway).search("abc", 3);
        verify(itemRepository).findAllById(List.of("id1", "id2"));
    }

    @Test
    void testSearchTranslatedRestrictedDelegatesToSearch() {
        Item item = new Item();
        when(searchGateway.search("xyz", 2)).thenReturn(List.of("id123"));
        when(itemRepository.findAllById(List.of("id123"))).thenReturn(List.of(item));

        List<Item> result = searchService.searchTranslatedRestricted("xyz", 2);

        assertThat(result).containsExactly(item);
    }

}
