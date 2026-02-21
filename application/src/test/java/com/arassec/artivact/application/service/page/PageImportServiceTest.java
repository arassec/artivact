package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ImportItemUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.application.port.in.page.UpdatePageAliasUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.domain.model.page.widget.TextWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILENAME_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class PageImportServiceTest {

    @InjectMocks
    private PageImportService service;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ImportItemUseCase importItemUseCase;

    @Mock
    private SavePageContentUseCase savePageContentUseCase;

    @Mock
    private UpdatePageAliasUseCase updatePageAliasUseCase;

    @Test
    void testImportPageWithItemSearchWidgetImportsItems() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        ItemSearchWidget itemSearchWidget = new ItemSearchWidget();
        itemSearchWidget.setId("widget-1");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>(List.of(itemSearchWidget)));

        Path pageDir = Path.of("page-dir");
        Path pageContentJson = pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON);
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-1"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageContentJson)).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        Path searchResultJson = Path.of("widget-dir").resolve(SEARCH_RESULT_FILENAME_JSON);
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.WIDGETS_DIR), "widget-1"))
                .thenReturn(Path.of("widget-dir"));
        when(fileRepository.exists(searchResultJson)).thenReturn(true);
        when(fileRepository.read(searchResultJson)).thenReturn("[\"item-1\",\"item-2\"]");
        when(jsonMapper.readValue(eq("[\"item-1\",\"item-2\"]"), any(TypeReference.class)))
                .thenReturn(List.of("item-1", "item-2"));

        // When
        service.importPage(importContext, "page-1", null);

        // Then
        verify(importItemUseCase).importItem(importContext, "item-1");
        verify(importItemUseCase).importItem(importContext, "item-2");
        verify(savePageContentUseCase).savePageContent(eq("page-1"), eq(Set.of()), eq(pageContent));
        verify(updatePageAliasUseCase, never()).updatePageAlias(any(), any());
    }

    @Test
    void testImportPageWithItemSearchWidgetSkipsWhenNoSearchResult() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        ItemSearchWidget itemSearchWidget = new ItemSearchWidget();
        itemSearchWidget.setId("widget-2");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>(List.of(itemSearchWidget)));

        Path pageDir = Path.of("page-dir");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-2"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        Path searchResultJson = Path.of("widget-dir").resolve(SEARCH_RESULT_FILENAME_JSON);
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.WIDGETS_DIR), "widget-2"))
                .thenReturn(Path.of("widget-dir"));
        when(fileRepository.exists(searchResultJson)).thenReturn(false);

        // When
        service.importPage(importContext, "page-2", "");

        // Then
        verify(importItemUseCase, never()).importItem(any(Path.class), any());
        verify(importItemUseCase, never()).importItem(any(ImportContext.class), any());
        verify(savePageContentUseCase).savePageContent(eq("page-2"), eq(Set.of()), eq(pageContent));
        verify(updatePageAliasUseCase, never()).updatePageAlias(any(), any());
    }

    @Test
    void testImportPageWithNonItemSearchWidgetCopiesFiles() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        TextWidget textWidget = new TextWidget();
        textWidget.setId("text-widget-1");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>(List.of(textWidget)));

        Path pageDir = Path.of("page-dir");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-3"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        Path widgetSource = Path.of("widget-source");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.WIDGETS_DIR), "text-widget-1"))
                .thenReturn(widgetSource);

        Path widgetTarget = Path.of("widget-target");
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project", DirectoryDefinitions.WIDGETS_DIR), "text-widget-1"))
                .thenReturn(widgetTarget);

        // When
        service.importPage(importContext, "page-3", null);

        // Then
        verify(fileRepository).copy(widgetSource, widgetTarget);
        verify(importItemUseCase, never()).importItem(any(Path.class), any());
        verify(importItemUseCase, never()).importItem(any(ImportContext.class), any());
        verify(savePageContentUseCase).savePageContent(eq("page-3"), eq(Set.of()), eq(pageContent));
    }

    @Test
    void testImportPageFiltersNullWidgets() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        List<Widget> widgets = new ArrayList<>();
        widgets.add(null);
        TextWidget textWidget = new TextWidget();
        textWidget.setId("valid-widget");
        widgets.add(textWidget);
        widgets.add(null);

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(widgets);

        Path pageDir = Path.of("page-dir");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-4"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.WIDGETS_DIR), "valid-widget"))
                .thenReturn(Path.of("widget-source"));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project", DirectoryDefinitions.WIDGETS_DIR), "valid-widget"))
                .thenReturn(Path.of("widget-target"));

        // When
        service.importPage(importContext, "page-4", null);

        // Then - only one widget should be processed (null ones filtered)
        verify(fileRepository, times(1)).copy(any(Path.class), any(Path.class));
        verify(savePageContentUseCase).savePageContent(eq("page-4"), eq(Set.of()), eq(pageContent));
    }

    @Test
    void testImportPageWithAliasUpdatesAlias() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>());

        Path pageDir = Path.of("page-dir");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-5"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        // When
        service.importPage(importContext, "page-5", "my-alias");

        // Then
        verify(updatePageAliasUseCase).updatePageAlias("page-5", "my-alias");
        verify(savePageContentUseCase).savePageContent(eq("page-5"), eq(Set.of()), eq(pageContent));
    }

    @Test
    void testImportPageWithMixedWidgets() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        ItemSearchWidget itemSearchWidget = new ItemSearchWidget();
        itemSearchWidget.setId("search-widget");

        TextWidget textWidget = new TextWidget();
        textWidget.setId("text-widget");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>(List.of(itemSearchWidget, textWidget)));

        Path pageDir = Path.of("page-dir");
        when(fileRepository.getDirFromId(Path.of("import", DirectoryDefinitions.PAGES_DIR), "page-6"))
                .thenReturn(pageDir);
        when(fileRepository.read(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON))).thenReturn("{}");
        when(jsonMapper.readValue(eq("{}"), eq(PageContent.class))).thenReturn(pageContent);

        // ItemSearchWidget setup
        Path searchWidgetDir = Path.of("search-widget-dir");
        Path searchResultJson = searchWidgetDir.resolve(SEARCH_RESULT_FILENAME_JSON);
        Path widgetsDir = Path.of("import", DirectoryDefinitions.WIDGETS_DIR);
        when(fileRepository.getDirFromId(widgetsDir, "search-widget"))
                .thenReturn(searchWidgetDir);
        when(fileRepository.exists(searchResultJson)).thenReturn(true);
        when(fileRepository.read(searchResultJson)).thenReturn("[\"item-x\"]");
        when(jsonMapper.readValue(eq("[\"item-x\"]"), any(TypeReference.class)))
                .thenReturn(List.of("item-x"));

        // TextWidget setup
        Path textWidgetSource = Path.of("text-widget-source");
        Path textWidgetTarget = Path.of("text-widget-target");
        when(fileRepository.getDirFromId(widgetsDir, "text-widget"))
                .thenReturn(textWidgetSource);
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project", DirectoryDefinitions.WIDGETS_DIR), "text-widget"))
                .thenReturn(textWidgetTarget);

        // When
        service.importPage(importContext, "page-6", "alias-6");

        // Then
        verify(importItemUseCase).importItem(importContext, "item-x");
        verify(fileRepository).copy(textWidgetSource, textWidgetTarget);
        verify(savePageContentUseCase).savePageContent(eq("page-6"), eq(Set.of()), eq(pageContent));
        verify(updatePageAliasUseCase).updatePageAlias("page-6", "alias-6");
    }
}
