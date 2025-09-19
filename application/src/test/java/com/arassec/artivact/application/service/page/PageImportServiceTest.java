package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ImportItemUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.application.port.in.page.UpdatePageAliasUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.widget.ItemSearchWidget;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILE_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageImportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

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

    @InjectMocks
    private PageImportService service;

    private ImportContext importContext;

    @BeforeEach
    void setUp() {
        importContext = new ImportContext();
        importContext.setImportDir(Path.of("/import"));
    }

    @Test
    void testImportPageBasic() throws Exception {
        String pageId = "page-1";
        String pageAlias = "alias-1";

        PageContent pageContent = new PageContent();
        when(fileRepository.read(importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX)))
                .thenReturn("page-content-json");
        when(objectMapper.readValue(eq("page-content-json"), eq(PageContent.class))).thenReturn(pageContent);

        service.importPage(importContext, pageId, pageAlias);

        verify(savePageContentUseCase).savePageContent(pageId, Set.of(), pageContent);
        verify(updatePageAliasUseCase).updatePageAlias(pageId, pageAlias);
    }

    @Test
    void testImportPageWithItemSearchWidget() throws Exception {
        String pageId = "page-1";

        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("widget1");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(List.of(widget));

        when(fileRepository.read(importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX)))
                .thenReturn("page-content-json");
        when(objectMapper.readValue(eq("page-content-json"), eq(PageContent.class))).thenReturn(pageContent);

        Path widgetSource = importContext.getImportDir().resolve(widget.getId());
        Path widgetTarget = Path.of("/widgets/widget1");
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("/project"));
        when(fileRepository.getDirFromId(any(), eq(widget.getId()))).thenReturn(widgetTarget);
        when(fileRepository.exists(importContext.getImportDir().resolve(widget.getId() + SEARCH_RESULT_FILE_SUFFIX))).thenReturn(true);
        when(fileRepository.read(importContext.getImportDir().resolve(widget.getId() + SEARCH_RESULT_FILE_SUFFIX)))
                .thenReturn("search-result");
        //noinspection unchecked
        when(objectMapper.readValue(eq("search-result"), any(TypeReference.class))).thenReturn(List.of("item1", "item2"));

        service.importPage(importContext, pageId, null);

        verify(fileRepository).copy(widgetSource, widgetTarget);
        verify(importItemUseCase).importItem(importContext, "item1");
        verify(importItemUseCase).importItem(importContext, "item2");
        verify(savePageContentUseCase).savePageContent(pageId, Set.of(), pageContent);
        verify(updatePageAliasUseCase, never()).updatePageAlias(anyString(), anyString());
    }

    @Test
    void testImportPageThrowsArtivactExceptionOnJsonProcessing() throws Exception {
        String pageId = "page-1";

        when(fileRepository.read(importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX)))
                .thenReturn("page-content-json");
        when(objectMapper.readValue(eq("page-content-json"), eq(PageContent.class)))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("error") {
                });

        assertThrows(ArtivactException.class, () -> service.importPage(importContext, pageId, null));
    }

    @Test
    void testImportPageSkipsNullWidgets() throws Exception {
        String pageId = "page-1";
        PageContent pageContent = new PageContent();

        List<Widget> widgets = new ArrayList<>();
        widgets.add(null);
        pageContent.setWidgets(widgets);

        when(fileRepository.read(importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX)))
                .thenReturn("page-content-json");
        when(objectMapper.readValue(eq("page-content-json"), eq(PageContent.class))).thenReturn(pageContent);

        service.importPage(importContext, pageId, null);

        assertThat(pageContent.getWidgets()).isEmpty();
    }

}
