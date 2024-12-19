package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.domain.service.SearchService;
import com.arassec.artivact.web.model.OperationProgress;
import com.arassec.artivact.web.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link SearchController}.
 */
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private SearchController controller;

    /**
     * Service mock.
     */
    @Mock
    private SearchService searchService;

    /**
     * Test progress expected from service calls.
     */
    private final OperationProgress expectedOperationProgress = OperationProgress.builder()
            .key("Progress.SearchControllerTest.test")
            .currentAmount(0)
            .targetAmount(0)
            .build();

    /**
     * Tests re-creating the search index.
     */
    @Test
    void testRecreateIndex() {
        ProgressMonitor progressMonitor = new ProgressMonitor(SearchControllerTest.class, "test");
        when(searchService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.recreateIndex();

        verify(searchService, times(1)).recreateIndex();
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests getting progress.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(SearchControllerTest.class, "test");
        when(searchService.getProgressMonitor()).thenReturn(progressMonitor);
        ResponseEntity<OperationProgress> responseEntity = controller.recreateIndex();

        verify(searchService, times(1)).getProgressMonitor();
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests searching.
     */
    @Test
    void testSearch() {
        List<Item> searchResult = List.of(
                createItem("one"),
                createItem("two"),
                createItem("three"),
                createItem("four"),
                createItem("five")
        );

        when(searchService.searchTranslatedRestricted("query", 5)).thenReturn(searchResult);

        SearchResult result = controller.search("query", 1, 2, 5);

        assertEquals(2, result.getPageSize());
        assertEquals("three", result.getData().get(0).getItemId());
        assertEquals("four", result.getData().get(1).getItemId());

        assertEquals(3, result.getTotalPages());
    }

    /**
     * Tests searching with an empty result.
     */
    @Test
    void testSearchEmptyResult() {
        when(searchService.searchTranslatedRestricted("query", 5)).thenReturn(List.of());

        SearchResult result = controller.search("query", 1, 2, 5);

        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getData()).isEmpty();
    }

    /**
     * Creates an item.
     *
     * @param id The item's ID.
     * @return The newly created item instance.
     */
    private Item createItem(String id) {
        Item item = new Item();
        item.setId(id);
        return item;
    }

}
