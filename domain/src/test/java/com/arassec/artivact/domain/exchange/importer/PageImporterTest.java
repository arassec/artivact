package com.arassec.artivact.domain.exchange.importer;

import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.PageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PageImporter}.
 */
@ExtendWith(MockitoExtension.class)
class PageImporterTest {

    /**
     * The importer under test.
     */
    @InjectMocks
    private PageImporter pageImporter;

    /**
     * Provider for project data.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Service for page handling.
     */
    @Mock
    private PageService pageService;

    /**
     * Service for item handling.
     */
    @Mock
    private ItemImporter itemImporter;

    /**
     * Import context for testing.
     */
    private final ImportContext importContext = ImportContext.builder()
            .importDir(Path.of("import-dir"))
            .build();

    /**
     * Tests importing a page.
     */
    @SuppressWarnings("unchecked")
    @Test
    @SneakyThrows
    void testImportPage() {
        ItemSearchWidget itemSearchWidget = new ItemSearchWidget();
        itemSearchWidget.setId("widget-id");

        PageContent pageContent = new PageContent();
        pageContent.setId("page-id");
        pageContent.getWidgets().add(itemSearchWidget);

        when(fileRepository.read(Path.of("import-dir/page-id.artivact.page-content.json"))).thenReturn("page-content-json");
        when(objectMapper.readValue("page-content-json", PageContent.class)).thenReturn(pageContent);

        Path widgetSource = importContext.getImportDir().resolve("widget-id");
        Path widgetTarget = Path.of("root/widgets/wid/get/widget-id");

        when(projectDataProvider.getProjectRoot()).thenReturn(Path.of("root"));
        when(fileRepository.getDirFromId(any(Path.class), eq("widget-id"))).thenReturn(widgetTarget);

        Path searchResultJson = Path.of("import-dir/widget-id.artivact.search-result.json");
        when(fileRepository.exists(searchResultJson)).thenReturn(true);
        when(fileRepository.read(searchResultJson)).thenReturn("search-result-json");
        when(objectMapper.readValue(eq("search-result-json"), any(TypeReference.class))).thenReturn(List.of("item-id"));

        pageImporter.importPage(importContext, "page-id", "page-alias");

        verify(fileRepository).copyDir(widgetSource, widgetTarget);
        verify(itemImporter).importItem(importContext, "item-id");

        verify(pageService).savePageContent("page-id", Set.of(), pageContent);
        verify(pageService).updatePageAlias("page-id", "page-alias");
    }

}
