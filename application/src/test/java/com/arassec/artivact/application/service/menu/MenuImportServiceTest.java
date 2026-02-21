package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.ImportPageUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.MENU_EXCHANGE_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuImportServiceTest {

    @InjectMocks
    private MenuImportService service;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ImportPageUseCase importPageUseCase;

    @Mock
    private SaveMenuUseCase saveMenuUseCase;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    // --- importMenu(Path) ---

    @Test
    void testImportMenuFromPathWithMenuContentSource() {
        // Given
        Path contentExport = Path.of("menu-export.artivact.collection.zip");
        Path importDir = Path.of("temp", "menu-export.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.MENU);
        exchangeMainData.setSourceIds(List.of("menu-1"));

        when(jsonMapper.readValue(
                eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()),
                eq(ExchangeMainData.class)
        )).thenReturn(exchangeMainData);

        Menu menu = createMenu("menu-1", null, List.of());
        mockMenuRead(importDir, "menu-1", menu);

        // When
        service.importMenu(contentExport);

        // Then
        verify(fileRepository).unpack(contentExport, importDir);
        verify(saveMenuUseCase).saveMenu(menu);
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(fileRepository).delete(importDir);
    }

    @Test
    void testImportMenuFromPathWithCollectionContentSourceAndNonEmptySourceIds() {
        // Given
        Path contentExport = Path.of("collection-export.artivact.collection.zip");
        Path importDir = Path.of("temp", "collection-export.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.COLLECTION);
        exchangeMainData.setSourceIds(List.of("menu-a", "menu-b"));

        when(jsonMapper.readValue(
                eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()),
                eq(ExchangeMainData.class)
        )).thenReturn(exchangeMainData);

        Menu menuA = createMenu("menu-a", null, List.of());
        Menu menuB = createMenu("menu-b", null, List.of());
        mockMenuRead(importDir, "menu-a", menuA);
        mockMenuRead(importDir, "menu-b", menuB);

        // When
        service.importMenu(contentExport);

        // Then
        verify(saveMenuUseCase).saveMenu(menuA);
        verify(saveMenuUseCase).saveMenu(menuB);
        verify(fileRepository).delete(importDir);
    }

    @Test
    void testImportMenuFromPathFailsWithInvalidContentSource() {
        // Given
        Path contentExport = Path.of("invalid.artivact.collection.zip");
        Path importDir = Path.of("temp", "invalid.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.ITEM);
        exchangeMainData.setSourceIds(List.of());

        when(jsonMapper.readValue(
                eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()),
                eq(ExchangeMainData.class)
        )).thenReturn(exchangeMainData);

        // When / Then
        assertThatThrownBy(() -> service.importMenu(contentExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not import data!");
    }

    @Test
    void testImportMenuFromPathFailsWithCollectionContentSourceAndEmptySourceIds() {
        // Given
        Path contentExport = Path.of("empty-collection.artivact.collection.zip");
        Path importDir = Path.of("temp", "empty-collection.artivact.collection");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));

        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(ContentSource.COLLECTION);
        exchangeMainData.setSourceIds(List.of());

        when(jsonMapper.readValue(
                eq(importDir.resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile()),
                eq(ExchangeMainData.class)
        )).thenReturn(exchangeMainData);

        // When / Then
        assertThatThrownBy(() -> service.importMenu(contentExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not import data!");
    }

    @Test
    void testImportMenuFromPathWrapsExceptionInArtivactException() {
        // Given
        Path contentExport = Path.of("failing.artivact.collection.zip");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));
        when(jsonMapper.readValue(any(File.class), eq(ExchangeMainData.class)))
                .thenThrow(new RuntimeException("parse error"));

        // When / Then
        assertThatThrownBy(() -> service.importMenu(contentExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not import data!");
    }

    // --- importMenu(ImportContext, String, boolean) ---

    @Test
    void testImportMenuWithSaveMenuTrue() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("menu-save", "old-parent", List.of());
        mockMenuRead(Path.of("import"), "menu-save", menu);

        // When
        service.importMenu(importContext, "menu-save", true);

        // Then
        assertThat(menu.getParentId()).isNull();
        verify(saveMenuUseCase).saveMenu(menu);
    }

    @Test
    void testImportMenuWithSaveMenuFalse() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("menu-no-save", "parent-id", List.of());
        mockMenuRead(Path.of("import"), "menu-no-save", menu);

        // When
        service.importMenu(importContext, "menu-no-save", false);

        // Then
        verify(saveMenuUseCase, never()).saveMenu(any());
    }

    @Test
    void testImportMenuWithSubMenusImportsRecursively() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu subMenu1 = createMenu("sub-1", null, List.of());
        Menu subMenu2 = createMenu("sub-2", null, List.of());

        Menu rootMenu = createMenu("root", null, List.of(subMenu1, subMenu2));
        mockMenuRead(Path.of("import"), "root", rootMenu);
        mockMenuRead(Path.of("import"), "sub-1", createMenu("sub-1", null, List.of()));
        mockMenuRead(Path.of("import"), "sub-2", createMenu("sub-2", null, List.of()));

        // When
        service.importMenu(importContext, "root", true);

        // Then
        assertThat(subMenu1.getParentId()).isEqualTo("root");
        assertThat(subMenu2.getParentId()).isEqualTo("root");
        verify(saveMenuUseCase).saveMenu(rootMenu);
        verify(saveMenuUseCase, times(1)).saveMenu(any());
    }

    @Test
    void testImportMenuWithEmptySubMenusDoesNotRecurse() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("leaf-menu", null, List.of());
        mockMenuRead(Path.of("import"), "leaf-menu", menu);

        // When
        service.importMenu(importContext, "leaf-menu", false);

        // Then
        verify(fileRepository, times(1)).getDirFromId(any(), any());
        verify(fileRepository, times(1)).read(any());
    }

    @Test
    void testImportMenuWithTargetPageIdImportsPage() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("menu-with-page", null, List.of());
        menu.setTargetPageId("page-99");
        menu.setTargetPageAlias("my-alias");
        mockMenuRead(Path.of("import"), "menu-with-page", menu);

        // When
        service.importMenu(importContext, "menu-with-page", false);

        // Then
        verify(importPageUseCase).importPage(importContext, "page-99", "my-alias");
    }

    @Test
    void testImportMenuWithoutTargetPageIdDoesNotImportPage() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("menu-no-page", null, List.of());
        menu.setTargetPageId(null);
        mockMenuRead(Path.of("import"), "menu-no-page", menu);

        // When
        service.importMenu(importContext, "menu-no-page", false);

        // Then
        verify(importPageUseCase, never()).importPage(any(), any(), any());
    }

    @Test
    void testImportMenuWithEmptyTargetPageIdDoesNotImportPage() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu menu = createMenu("menu-empty-page", null, List.of());
        menu.setTargetPageId("");
        mockMenuRead(Path.of("import"), "menu-empty-page", menu);

        // When
        service.importMenu(importContext, "menu-empty-page", false);

        // Then
        verify(importPageUseCase, never()).importPage(any(), any(), any());
    }

    @Test
    void testImportMenuWithNestedSubMenusSetsCorrectParentIds() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Menu leaf = createMenu("leaf", null, List.of());
        Menu mid = createMenu("mid", null, List.of(leaf));
        Menu root = createMenu("root", "old-parent", List.of(mid));

        mockMenuRead(Path.of("import"), "root", root);

        Menu midRead = createMenu("mid", null, List.of(leaf));
        mockMenuRead(Path.of("import"), "mid", midRead);

        Menu leafRead = createMenu("leaf", null, List.of());
        mockMenuRead(Path.of("import"), "leaf", leafRead);

        // When
        service.importMenu(importContext, "root", true);

        // Then
        assertThat(root.getParentId()).isNull();
        assertThat(mid.getParentId()).isEqualTo("root");
        assertThat(leaf.getParentId()).isEqualTo("mid");
        verify(saveMenuUseCase, times(1)).saveMenu(root);
    }

    // --- Helper methods ---

    private Menu createMenu(String id, String parentId, List<Menu> menuEntries) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuEntries(menuEntries);
        return menu;
    }

    private void mockMenuRead(Path importDir, String menuId, Menu menu) {
        Path menuDir = Path.of("menu-dir-" + menuId);
        Path menuJson = menuDir.resolve(MENU_EXCHANGE_FILENAME_JSON);

        when(fileRepository.getDirFromId(importDir.resolve(DirectoryDefinitions.MENUS_DIR), menuId))
                .thenReturn(menuDir);

        String json = "{\"id\":\"" + menuId + "\"}";
        when(fileRepository.read(menuJson)).thenReturn(json);
        when(jsonMapper.readValue(json, Menu.class)).thenReturn(menu);
    }
}
