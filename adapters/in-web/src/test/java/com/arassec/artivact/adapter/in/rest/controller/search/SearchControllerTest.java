package com.arassec.artivact.adapter.in.rest.controller.search;

import com.arassec.artivact.adapter.in.rest.model.SearchResult;
import com.arassec.artivact.application.port.in.search.ManageSearchIndexUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SearchController}.
 */
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private SearchController searchController;

    /**
     * Mock for search index management.
     */
    @Mock
    private ManageSearchIndexUseCase manageSearchIndexUseCase;

    /**
     * Mock for item searches.
     */
    @Mock
    private SearchItemsUseCase searchItemsUseCase;

    /**
     * Tests re-creating the search index.
     */
    @Test
    void testRecreateIndex() {
        manageSearchIndexUseCase.recreateIndex();
        verify(manageSearchIndexUseCase).recreateIndex();
    }

    /**
     * Tests item search.
     */
    @Test
    void testSearch() {
        SearchResult searchResult = searchController.search("item", 0, 1, 1);
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getData()).isEmpty();

        Item itemOne = new Item();
        itemOne.setTitle(new TranslatableString("itemOne"));
        Item itemTwo = new Item();
        itemTwo.setId("itemId");
        itemTwo.setTitle(new TranslatableString("itemTwo"));
        itemTwo.getMediaContent().getImages().add("image.jpg");
        Item itemThree = new Item();
        itemThree.setTitle(new TranslatableString("itemThree"));

        when(searchItemsUseCase.searchTranslatedRestricted("item", 100)).thenReturn(List.of(
                itemOne, itemTwo, itemThree
        ));


        searchResult = searchController.search("item", 1, 1, 100);

        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getData()).hasSize(1);
        assertThat(searchResult.getData().getFirst().getTitle().getValue()).isEqualTo("itemTwo");
        assertThat(searchResult.getData().getFirst().getImageUrl()).isEqualTo("/api/item/itemId/image/image.jpg");
    }

}
