package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.ExhibitionSummary;
import com.arassec.artivact.backend.service.ExhibitionService;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.exhibition.Exhibition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExhibitionController}.
 */
@ExtendWith(MockitoExtension.class)
class ExhibitionControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ExhibitionController exhibitionController;

    /**
     * The application's {@link ExhibitionService}.
     */
    @Mock
    private ExhibitionService exhibitionService;

    /**
     * Tests getting all available exhibitions.
     */
    @Test
    void testRead() {
        Exhibition exhibition = new Exhibition();
        exhibition.setId("123-abc");
        exhibition.setVersion(1);
        exhibition.setTitle(TranslatableString.builder().value("title").build());
        exhibition.setDescription(TranslatableString.builder().value("description").build());
        exhibition.setReferencedMenuIds(List.of("referenced-menu-id"));

        when(exhibitionService.loadAll()).thenReturn(List.of(exhibition));

        ResponseEntity<List<ExhibitionSummary>> response = exhibitionController.read();

        List<ExhibitionSummary> exhibitions = response.getBody();
        assertNotNull(exhibitions);
        assertEquals(1, exhibitions.size());
        assertEquals("123-abc", exhibitions.getFirst().getExhibitionId());
        assertEquals("title", exhibitions.getFirst().getTitle().getValue());
        assertEquals("description", exhibitions.getFirst().getDescription().getValue());
        assertEquals(1, exhibitions.getFirst().getMenuIds().size());
        assertEquals("referenced-menu-id", exhibitions.getFirst().getMenuIds().getFirst());
    }

    /**
     * Tests saving an exhibition.
     */
    @Test
    void testSave() {
        TranslatableString title = TranslatableString.builder().value("title").build();
        TranslatableString description = TranslatableString.builder().value("description").build();

        List<String> menuIds = List.of("menu-id");

        ExhibitionSummary exhibitionSummary = new ExhibitionSummary();
        exhibitionSummary.setExhibitionId("exhibition-id");
        exhibitionSummary.setTitle(title);
        exhibitionSummary.setDescription(description);
        exhibitionSummary.setMenuIds(menuIds);

        exhibitionController.save(exhibitionSummary);

        verify(exhibitionService, times(1)).createOrUpdate("exhibition-id", title, description, menuIds);
        verify(exhibitionService, times(1)).getProgressMonitor();
    }

    /**
     * Tests deleting an exhibition.
     */
    @Test
    void testDelete() {
        exhibitionController.delete("123-abc");
        verify(exhibitionService, times(1)).delete("123-abc");
    }

    /**
     * Tests getting the progress of a long-running operation.
     */
    @Test
    void testGetProgress() {
        exhibitionController.getProgress();
        verify(exhibitionService, times(1)).getProgressMonitor();
    }

}
