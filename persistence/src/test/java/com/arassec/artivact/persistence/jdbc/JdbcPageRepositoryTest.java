package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.widget.TextWidget;
import com.arassec.artivact.core.repository.PageIdAndAlias;
import com.arassec.artivact.persistence.jdbc.springdata.entity.PageEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.PageEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcPageRepository}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcPageRepositoryTest {

    /**
     * The repository under test.
     */
    @InjectMocks
    private JdbcPageRepository jdbcPageRepository;

    /**
     * Spring-Data's page repository mock.
     */
    @Mock
    private PageEntityRepository pageEntityRepository;

    /**
     * ObjectMapper mock.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests saving a new page.
     */
    @Test
    @SneakyThrows
    void testSaveNew() {
        Page page = new Page();
        page.setId("id");
        page.setPageContent(new PageContent());

        when(objectMapper.writeValueAsString(any(PageContent.class))).thenReturn("{contentJson}");

        jdbcPageRepository.save(page);

        ArgumentCaptor<PageEntity> argCap = ArgumentCaptor.forClass(PageEntity.class);

        verify(pageEntityRepository, times(1)).save(argCap.capture());

        PageEntity pageEntity = argCap.getValue();

        assertEquals("id", pageEntity.getId());
        assertEquals("{contentJson}", pageEntity.getContentJson());
    }

    /**
     * Tests updating an existing page.
     */
    @Test
    @SneakyThrows
    void testSaveExisting() {
        Page page = new Page();
        page.setId("id");
        page.setPageContent(new PageContent());
        page.getPageContent().setIndexPage(true);

        when(objectMapper.writeValueAsString(any(PageContent.class))).thenReturn("{contentJson}");

        PageEntity pageEntity = new PageEntity();

        when(pageEntityRepository.findById("id")).thenReturn(Optional.of(pageEntity));

        jdbcPageRepository.save(page);

        assertEquals("{contentJson}", pageEntity.getContentJson());
        assertTrue(pageEntity.isIndexPage());
    }

    /**
     * Tests deleting a page by its ID.
     */
    @Test
    @SneakyThrows
    void testDeleteById() {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setId("id");
        pageEntity.setContentJson("{contentJson}");

        when(pageEntityRepository.findById("id")).thenReturn(Optional.of(pageEntity));

        PageContent pageContent = new PageContent();

        when(objectMapper.readValue("{contentJson}", PageContent.class)).thenReturn(pageContent);

        Optional<Page> pageOptional = jdbcPageRepository.deleteById("id");

        assertTrue(pageOptional.isPresent());
        assertEquals("id", pageOptional.get().getId());

        verify(pageEntityRepository, times(1)).deleteById("id");
    }

    /**
     * Tests finding all available pages.
     */
    @Test
    @SneakyThrows
    void testFindAll() {
        when(pageEntityRepository.findAll()).thenReturn(List.of(
                new PageEntity(), new PageEntity()
        ));

        List<Page> pages = jdbcPageRepository.findAll();

        assertThat(pages).hasSize(2);
    }

    /**
     * Tests finding a page by its ID.
     */
    @Test
    @SneakyThrows
    void testFindById() {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setId("id");
        pageEntity.setContentJson("{contentJson}");

        when(pageEntityRepository.findById("id")).thenReturn(Optional.of(pageEntity));

        PageContent pageContent = new PageContent();
        pageContent.getWidgets().add(new TextWidget());
        pageContent.getWidgets().add(null); // must be removed!
        pageContent.getWidgets().add(new TextWidget());

        when(objectMapper.readValue("{contentJson}", PageContent.class)).thenReturn(pageContent);

        Optional<Page> pageOptional = jdbcPageRepository.deleteById("id");

        assertTrue(pageOptional.isPresent());

        Page page = pageOptional.get();
        assertEquals("id", page.getId());
        assertEquals(2, page.getPageContent().getWidgets().size());
    }

    /**
     * Tests finding the index page which doesn't exist.
     */
    @Test
    void testFindIndexPageIdNotExisting() {
        Optional<PageIdAndAlias> indexPageIdOptional = jdbcPageRepository.findIndexPageId();
        assertThat(indexPageIdOptional).isEmpty();
    }

    /**
     * Tests finding the index page.
     */
    @Test
    @SneakyThrows
    void testFindIndexPageId() {
        when(pageEntityRepository.findFirstIndexPageId(true)).thenReturn(Optional.of(new PageIdAndAlias() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getAlias() {
                return "alias";
            }
        }));

        Optional<PageIdAndAlias> indexPageIdOptional = jdbcPageRepository.findIndexPageId();
        assertThat(indexPageIdOptional).isPresent();
        assertThat(indexPageIdOptional.get().getId()).isEqualTo("id");
        assertThat(indexPageIdOptional.get().getAlias()).isEqualTo("alias");
    }

}
