package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PAGE_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.SEARCH_RESULT_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageExportServiceTest {

    @InjectMocks
    private PageExportService service;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ExportItemUseCase exportItemUseCase;

    @Mock
    private SearchItemsUseCase searchItemsUseCase;

    private final Path exportDir = Path.of("export");

    private ExportContext createContext(boolean applyRestrictions, boolean excludeItems) {
        return ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(applyRestrictions)
                        .excludeItems(excludeItems)
                        .build())
                .build();
    }

    private ExportContext createContext(boolean applyRestrictions) {
        return createContext(applyRestrictions, false);
    }

    private PageContent createPageContent(String id, Set<String> restrictions, com.arassec.artivact.domain.model.page.Widget... widgets) {
        PageContent content = new PageContent();
        content.setId(id);
        content.setRestrictions(restrictions);
        content.setWidgets(List.of(widgets));
        return content;
    }

    @BeforeEach
    void setUp() {
        lenient().when(fileRepository.getDirFromId(eq(exportDir.resolve(DirectoryDefinitions.PAGES_DIR)), anyString()))
                .thenReturn(Path.of("page-export-dir"));
    }

    // --- Page-level restriction tests ---

    @Test
    void testExportPageSkipsRestrictedPage() {
        // Given
        PageContent content = createPageContent("page-1", Set.of("ROLE_ADMIN"));
        ExportContext ctx = createContext(true);

        // When
        service.exportPage(ctx, "page-1", content);

        // Then
        verify(fileRepository, never()).createDirIfRequired(any());
        verify(jsonMapper, never()).writeValue(any(File.class), any());
    }

    @Test
    void testExportPageExportsUnrestrictedPage() {
        // Given
        PageContent content = createPageContent("page-2", Set.of());
        ExportContext ctx = createContext(true);

        // When
        service.exportPage(ctx, "page-2", content);

        // Then
        verify(fileRepository).createDirIfRequired(Path.of("page-export-dir"));
        verify(jsonMapper).writeValue(
                eq(Path.of("page-export-dir").resolve(PAGE_EXCHANGE_FILENAME_JSON).toFile()),
                eq(content)
        );
    }

    @Test
    void testExportPageIgnoresRestrictionsWhenNotApplied() {
        // Given
        PageContent content = createPageContent("page-3", Set.of("ROLE_ADMIN"));
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-3", content);

        // Then
        verify(jsonMapper).writeValue(any(File.class), eq(content));
    }

    // --- Widget-level restriction tests ---

    @Test
    void testExportPageSkipsRestrictedWidget() {
        // Given
        TextWidget restricted = new TextWidget();
        restricted.setId("w-restricted");
        restricted.setRestrictions(Set.of("ROLE_ADMIN"));
        restricted.setHeading(createTranslatableString("heading", "translated"));
        restricted.setContent(createTranslatableString("content", "translated"));

        PageContent content = createPageContent("page-4", Set.of(), restricted);
        ExportContext ctx = createContext(true);

        // When
        service.exportPage(ctx, "page-4", content);

        // Then - translations should NOT be cleaned (widget was skipped)
        assertThat(restricted.getHeading().getTranslatedValue()).isEqualTo("translated");
    }

    @Test
    void testExportPageExportsWidgetWithEmptyRestrictions() {
        // Given
        TextWidget widget = new TextWidget();
        widget.setId("w-unrestricted");
        widget.setRestrictions(Set.of());
        widget.setHeading(createTranslatableString("heading", "translated"));
        widget.setContent(createTranslatableString("content", "translated"));

        PageContent content = createPageContent("page-5", Set.of(), widget);
        ExportContext ctx = createContext(true);

        // When
        service.exportPage(ctx, "page-5", content);

        // Then
        assertThat(widget.getHeading().getTranslatedValue()).isNull();
        assertThat(widget.getContent().getTranslatedValue()).isNull();
    }

    // --- NavigationTitle cleanup ---

    @Test
    void testExportPageClearsNavigationTitleTranslation() {
        // Given
        TextWidget widget = new TextWidget();
        widget.setId("w-nav");
        widget.setRestrictions(Set.of());
        widget.setNavigationTitle(createTranslatableString("nav", "translated-nav"));
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-nav", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-nav", content);

        // Then
        assertThat(widget.getNavigationTitle().getTranslatedValue()).isNull();
    }

    @Test
    void testExportPageHandlesNullNavigationTitle() {
        // Given
        TextWidget widget = new TextWidget();
        widget.setId("w-null-nav");
        widget.setRestrictions(Set.of());
        widget.setNavigationTitle(null);
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-null-nav", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-null-nav", content);

        // Then - no exception
        assertThat(widget.getNavigationTitle()).isNull();
    }

    // --- AvatarWidget ---

    @Test
    void testExportPageWithAvatarWidgetCopiesImage() {
        // Given
        AvatarWidget widget = new AvatarWidget();
        widget.setId("avatar-1");
        widget.setRestrictions(Set.of());
        widget.setAvatarImage("photo.jpg");
        widget.setAvatarSubtext(createTranslatableString("subtext", "translated"));

        PageContent content = createPageContent("page-avatar", Set.of(), widget);
        ExportContext ctx = createContext(false);

        Path sourceDir = Path.of("source-widget-dir");
        Path targetDir = Path.of("target-widget-dir");

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project").resolve(DirectoryDefinitions.WIDGETS_DIR), "avatar-1"))
                .thenReturn(sourceDir);
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "avatar-1"))
                .thenReturn(targetDir);

        // When
        service.exportPage(ctx, "page-avatar", content);

        // Then
        verify(fileRepository).createDirIfRequired(targetDir);
        verify(fileRepository).copy(sourceDir.resolve("photo.jpg"), targetDir.resolve("photo.jpg"));
        assertThat(widget.getAvatarSubtext().getTranslatedValue()).isNull();
    }

    @Test
    void testExportPageWithAvatarWidgetWithoutImageDoesNotCopy() {
        // Given
        AvatarWidget widget = new AvatarWidget();
        widget.setId("avatar-empty");
        widget.setRestrictions(Set.of());
        widget.setAvatarImage("");
        widget.setAvatarSubtext(createTranslatableString("sub", null));

        PageContent content = createPageContent("page-avatar-empty", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-avatar-empty", content);

        // Then
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
    }

    @Test
    void testExportPageWithAvatarWidgetNullImageDoesNotCopy() {
        // Given
        AvatarWidget widget = new AvatarWidget();
        widget.setId("avatar-null");
        widget.setRestrictions(Set.of());
        widget.setAvatarImage(null);
        widget.setAvatarSubtext(createTranslatableString("sub", null));

        PageContent content = createPageContent("page-avatar-null", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-avatar-null", content);

        // Then
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
    }

    // --- InfoBoxWidget ---

    @Test
    void testExportPageWithInfoBoxWidgetCleansTranslations() {
        // Given
        InfoBoxWidget widget = new InfoBoxWidget();
        widget.setId("info-1");
        widget.setRestrictions(Set.of());
        widget.setHeading(createTranslatableString("heading", "t-heading"));
        widget.setContent(createTranslatableString("content", "t-content"));

        PageContent content = createPageContent("page-info", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-info", content);

        // Then
        assertThat(widget.getHeading().getTranslatedValue()).isNull();
        assertThat(widget.getContent().getTranslatedValue()).isNull();
    }

    // --- PageTitleWidget ---

    @Test
    void testExportPageWithPageTitleWidgetCopiesBackground() {
        // Given
        PageTitleWidget widget = new PageTitleWidget();
        widget.setId("title-1");
        widget.setRestrictions(Set.of());
        widget.setBackgroundImage("bg.png");
        widget.setTitle(createTranslatableString("title", "t-title"));

        PageContent content = createPageContent("page-title", Set.of(), widget);
        ExportContext ctx = createContext(false);

        Path sourceDir = Path.of("source-title");
        Path targetDir = Path.of("target-title");

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project").resolve(DirectoryDefinitions.WIDGETS_DIR), "title-1"))
                .thenReturn(sourceDir);
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "title-1"))
                .thenReturn(targetDir);

        // When
        service.exportPage(ctx, "page-title", content);

        // Then
        verify(fileRepository).copy(sourceDir.resolve("bg.png"), targetDir.resolve("bg.png"));
        assertThat(widget.getTitle().getTranslatedValue()).isNull();
    }

    @Test
    void testExportPageWithPageTitleWidgetWithoutBackgroundDoesNotCopy() {
        // Given
        PageTitleWidget widget = new PageTitleWidget();
        widget.setId("title-no-bg");
        widget.setRestrictions(Set.of());
        widget.setBackgroundImage(null);
        widget.setTitle(createTranslatableString("title", null));

        PageContent content = createPageContent("page-title-no-bg", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-title-no-bg", content);

        // Then
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
    }

    // --- TextWidget ---

    @Test
    void testExportPageWithTextWidgetCleansTranslations() {
        // Given
        TextWidget widget = new TextWidget();
        widget.setId("text-1");
        widget.setRestrictions(Set.of());
        widget.setHeading(createTranslatableString("h", "t-h"));
        widget.setContent(createTranslatableString("c", "t-c"));

        PageContent content = createPageContent("page-text", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-text", content);

        // Then
        assertThat(widget.getHeading().getTranslatedValue()).isNull();
        assertThat(widget.getContent().getTranslatedValue()).isNull();
    }

    // --- ImageGalleryWidget ---

    @Test
    void testExportPageWithImageGalleryWidgetCopiesAllImages() {
        // Given
        ImageGalleryWidget widget = new ImageGalleryWidget();
        widget.setId("gallery-1");
        widget.setRestrictions(Set.of());
        widget.setImages(List.of("img1.png", "img2.png", "img3.png"));
        widget.setHeading(createTranslatableString("h", "t-h"));
        widget.setContent(createTranslatableString("c", "t-c"));

        PageContent content = createPageContent("page-gallery", Set.of(), widget);
        ExportContext ctx = createContext(false);

        Path sourceDir = Path.of("source-gallery");
        Path targetDir = Path.of("target-gallery");

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project").resolve(DirectoryDefinitions.WIDGETS_DIR), "gallery-1"))
                .thenReturn(sourceDir);
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "gallery-1"))
                .thenReturn(targetDir);

        // When
        service.exportPage(ctx, "page-gallery", content);

        // Then
        verify(fileRepository).copy(sourceDir.resolve("img1.png"), targetDir.resolve("img1.png"));
        verify(fileRepository).copy(sourceDir.resolve("img2.png"), targetDir.resolve("img2.png"));
        verify(fileRepository).copy(sourceDir.resolve("img3.png"), targetDir.resolve("img3.png"));
        verify(fileRepository, times(3)).createDirIfRequired(targetDir);
        assertThat(widget.getHeading().getTranslatedValue()).isNull();
        assertThat(widget.getContent().getTranslatedValue()).isNull();
    }

    @Test
    void testExportPageWithImageGalleryWidgetWithEmptyImagesDoesNotCopy() {
        // Given
        ImageGalleryWidget widget = new ImageGalleryWidget();
        widget.setId("gallery-empty");
        widget.setRestrictions(Set.of());
        widget.setImages(Collections.emptyList());
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-gallery-empty", Set.of(), widget);
        ExportContext ctx = createContext(false);

        // When
        service.exportPage(ctx, "page-gallery-empty", content);

        // Then
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
    }

    // --- ItemSearchWidget ---

    @Test
    void testExportPageWithItemSearchWidgetExportsItems() {
        // Given
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search-1");
        widget.setRestrictions(Set.of());
        widget.setSearchTerm("query");
        widget.setMaxResults(10);
        widget.setHeading(createTranslatableString("h", "t-h"));
        widget.setContent(createTranslatableString("c", "t-c"));

        PageContent content = createPageContent("page-search", Set.of(), widget);
        ExportContext ctx = createContext(false, false);

        Item item1 = new Item();
        item1.setId("item-1");
        Item item2 = new Item();
        item2.setId("item-2");

        when(searchItemsUseCase.search("query", 10)).thenReturn(List.of(item1, item2));

        Path widgetExportDir = Path.of("widget-export");
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "search-1"))
                .thenReturn(widgetExportDir);

        // When
        service.exportPage(ctx, "page-search", content);

        // Then
        verify(exportItemUseCase).exportItem(ctx, item1);
        verify(exportItemUseCase).exportItem(ctx, item2);
        verify(fileRepository).createDirIfRequired(widgetExportDir);

        ArgumentCaptor<Object> jsonCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonMapper).writeValue(
                eq(widgetExportDir.resolve(SEARCH_RESULT_FILENAME_JSON).toFile()),
                jsonCaptor.capture()
        );
        Object[] exportedIds = (Object[]) jsonCaptor.getValue();
        assertThat(exportedIds).containsExactly("item-1", "item-2");

        assertThat(widget.getHeading().getTranslatedValue()).isNull();
        assertThat(widget.getContent().getTranslatedValue()).isNull();
    }

    @Test
    void testExportPageWithItemSearchWidgetExcludesItems() {
        // Given
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search-excluded");
        widget.setRestrictions(Set.of());
        widget.setSearchTerm("query");
        widget.setMaxResults(5);
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-search-excluded", Set.of(), widget);
        ExportContext ctx = createContext(false, true);

        // When
        service.exportPage(ctx, "page-search-excluded", content);

        // Then
        verify(searchItemsUseCase, never()).search(any(), anyInt());
        verify(exportItemUseCase, never()).exportItem(any(), any(Item.class));
    }

    @Test
    void testExportPageWithItemSearchWidgetFiltersRestrictedItems() {
        // Given
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search-filtered");
        widget.setRestrictions(Set.of());
        widget.setSearchTerm("all");
        widget.setMaxResults(10);
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-search-filtered", Set.of(), widget);
        ExportContext ctx = createContext(true, false);

        Item unrestricted = new Item();
        unrestricted.setId("item-open");
        unrestricted.setRestrictions(Set.of());

        Item restricted = new Item();
        restricted.setId("item-restricted");
        restricted.setRestrictions(Set.of("ROLE_ADMIN"));

        when(searchItemsUseCase.search("all", 10)).thenReturn(List.of(unrestricted, restricted));

        Path widgetExportDir = Path.of("widget-filtered");
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "search-filtered"))
                .thenReturn(widgetExportDir);

        // When
        service.exportPage(ctx, "page-search-filtered", content);

        // Then
        verify(exportItemUseCase).exportItem(ctx, unrestricted);
        verify(exportItemUseCase, never()).exportItem(ctx, restricted);

        ArgumentCaptor<Object> jsonCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonMapper).writeValue(
                eq(widgetExportDir.resolve(SEARCH_RESULT_FILENAME_JSON).toFile()),
                jsonCaptor.capture()
        );
        Object[] exportedIds = (Object[]) jsonCaptor.getValue();
        assertThat(exportedIds).containsExactly("item-open");
    }

    @Test
    void testExportPageWithItemSearchWidgetNullResultDoesNotExport() {
        // Given
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search-null");
        widget.setRestrictions(Set.of());
        widget.setSearchTerm("nothing");
        widget.setMaxResults(5);
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-search-null", Set.of(), widget);
        ExportContext ctx = createContext(false, false);

        when(searchItemsUseCase.search("nothing", 5)).thenReturn(null);

        // When
        service.exportPage(ctx, "page-search-null", content);

        // Then
        verify(exportItemUseCase, never()).exportItem(any(), any(Item.class));
        verify(jsonMapper, never()).writeValue(
                eq(Path.of("any").resolve(SEARCH_RESULT_FILENAME_JSON).toFile()),
                any()
        );
    }

    @Test
    void testExportPageWithItemSearchWidgetEmptyResultDoesNotExport() {
        // Given
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search-empty");
        widget.setRestrictions(Set.of());
        widget.setSearchTerm("empty");
        widget.setMaxResults(5);
        widget.setHeading(createTranslatableString("h", null));
        widget.setContent(createTranslatableString("c", null));

        PageContent content = createPageContent("page-search-empty", Set.of(), widget);
        ExportContext ctx = createContext(false, false);

        when(searchItemsUseCase.search("empty", 5)).thenReturn(Collections.emptyList());

        // When
        service.exportPage(ctx, "page-search-empty", content);

        // Then
        verify(exportItemUseCase, never()).exportItem(any(), any(Item.class));
    }

    // --- Mixed widgets ---

    @Test
    void testExportPageWithMultipleWidgetTypes() {
        // Given
        TextWidget text = new TextWidget();
        text.setId("w-text");
        text.setRestrictions(Set.of());
        text.setHeading(createTranslatableString("h", "t"));
        text.setContent(createTranslatableString("c", "t"));

        InfoBoxWidget info = new InfoBoxWidget();
        info.setId("w-info");
        info.setRestrictions(Set.of());
        info.setHeading(createTranslatableString("ih", "t"));
        info.setContent(createTranslatableString("ic", "t"));

        AvatarWidget avatar = new AvatarWidget();
        avatar.setId("w-avatar");
        avatar.setRestrictions(Set.of());
        avatar.setAvatarImage("avatar.png");
        avatar.setAvatarSubtext(createTranslatableString("sub", "t"));

        PageContent content = createPageContent("page-mixed", Set.of(), text, info, avatar);
        ExportContext ctx = createContext(false);

        Path sourceDir = Path.of("src-avatar");
        Path targetDir = Path.of("tgt-avatar");
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project"));
        when(fileRepository.getDirFromId(Path.of("project").resolve(DirectoryDefinitions.WIDGETS_DIR), "w-avatar"))
                .thenReturn(sourceDir);
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.WIDGETS_DIR), "w-avatar"))
                .thenReturn(targetDir);

        // When
        service.exportPage(ctx, "page-mixed", content);

        // Then
        assertThat(text.getHeading().getTranslatedValue()).isNull();
        assertThat(info.getHeading().getTranslatedValue()).isNull();
        assertThat(avatar.getAvatarSubtext().getTranslatedValue()).isNull();
        verify(fileRepository).copy(sourceDir.resolve("avatar.png"), targetDir.resolve("avatar.png"));
        verify(jsonMapper).writeValue(any(File.class), eq(content));
    }

    // --- JSON output ---

    @Test
    void testExportPageWritesPageContentJson() {
        // Given
        PageContent content = createPageContent("page-json", Set.of());
        ExportContext ctx = createContext(false);

        Path pageDir = Path.of("page-json-dir");
        when(fileRepository.getDirFromId(exportDir.resolve(DirectoryDefinitions.PAGES_DIR), "page-json"))
                .thenReturn(pageDir);

        // When
        service.exportPage(ctx, "page-json", content);

        // Then
        verify(fileRepository).createDirIfRequired(pageDir);
        verify(jsonMapper).writeValue(
                eq(pageDir.resolve(PAGE_EXCHANGE_FILENAME_JSON).toFile()),
                eq(content)
        );
    }

    // --- Helper ---

    private TranslatableString createTranslatableString(String value, String translatedValue) {
        TranslatableString ts = new TranslatableString();
        ts.setValue(value);
        ts.setTranslatedValue(translatedValue);
        return ts;
    }
}
