package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.page.ExportPageUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.page.PageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.MENU_EXCHANGE_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuExportServiceTest {

    @InjectMocks
    private MenuExportService service;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadPageContentUseCase loadPageContentUseCase;

    @Mock
    private ExportPageUseCase exportPageUseCase;

    @Mock
    private LoadMenuUseCase loadMenuUseCase;

    @Test
    void testExportMenuByIdCreatesExportFile() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-1");
        menu.setMenuEntries(List.of());

        when(loadMenuUseCase.loadMenu("menu-1")).thenReturn(menu);
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));

        Path menuExportDir = Path.of("menu-export-dir");
        when(fileRepository.getDirFromId(any(Path.class), eq("menu-1"))).thenReturn(menuExportDir);

        // When
        Path result = service.exportMenu("menu-1");

        // Then
        assertThat(result).isNotNull();
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());
    }

    @Test
    void testExportMenuWithoutRestrictionsKeepsAllEntries() {
        // Given
        Menu subMenu = new Menu();
        subMenu.setId("sub-1");
        subMenu.setRestrictions(Set.of(Roles.ROLE_ADMIN));
        subMenu.setMenuEntries(List.of());

        Menu menu = new Menu();
        menu.setId("menu-2");
        menu.setMenuEntries(new ArrayList<>(List.of(subMenu)));

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then - sub-menu is still present (not filtered)
        assertThat(menu.getMenuEntries()).hasSize(1);
        // Menu and sub-menu both exported
        verify(fileRepository, times(2)).createDirIfRequired(menuExportDir);
    }

    @Test
    void testExportMenuWithRestrictionsFiltersRestrictedEntries() {
        // Given
        Menu restrictedEntry = new Menu();
        restrictedEntry.setId("restricted");
        restrictedEntry.setRestrictions(Set.of(Roles.ROLE_ADMIN));
        restrictedEntry.setMenuEntries(List.of());

        Menu unrestrictedEntry = new Menu();
        unrestrictedEntry.setId("unrestricted");
        unrestrictedEntry.setRestrictions(Set.of());
        unrestrictedEntry.setMenuEntries(List.of());

        Menu menu = new Menu();
        menu.setId("menu-3");
        menu.setMenuEntries(new ArrayList<>(List.of(restrictedEntry, unrestrictedEntry)));

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(true)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then - only unrestricted entry remains
        assertThat(menu.getMenuEntries()).hasSize(1);
        assertThat(menu.getMenuEntries().getFirst().getId()).isEqualTo("unrestricted");
    }

    @Test
    void testExportMenuWithRestrictionsAndEmptyEntriesSetsNull() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-empty");
        menu.setMenuEntries(new ArrayList<>());

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(true)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then
        assertThat(menu.getMenuEntries()).isNull();
    }

    @Test
    void testExportMenuWithTargetPageIdExportsPage() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-page");
        menu.setTargetPageId("page-42");
        menu.setMenuEntries(List.of());

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        PageContent pageContent = new PageContent();
        when(loadPageContentUseCase.loadPageContent("page-42", Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER)))
                .thenReturn(pageContent);

        // When
        service.exportMenu(exportContext, menu);

        // Then
        verify(exportPageUseCase).exportPage(exportContext, "page-42", pageContent);
    }

    @Test
    void testExportMenuWithoutTargetPageIdDoesNotExportPage() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-no-page");
        menu.setTargetPageId(null);
        menu.setMenuEntries(List.of());

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then
        verify(loadPageContentUseCase, never()).loadPageContent(any(), any());
        verify(exportPageUseCase, never()).exportPage(any(), any(), any());
    }

    @Test
    void testExportMenuWithEmptyTargetPageIdDoesNotExportPage() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-empty-page");
        menu.setTargetPageId("");
        menu.setMenuEntries(List.of());

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then
        verify(loadPageContentUseCase, never()).loadPageContent(any(), any());
        verify(exportPageUseCase, never()).exportPage(any(), any(), any());
    }

    @Test
    void testExportMenuWithNullMenuEntriesDoesNotFail() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-null-entries");
        menu.setMenuEntries(null);

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then - no exception, JSON file written
        verify(jsonMapper).writeValue(any(File.class), eq(menu));
    }

    @Test
    void testExportMenuWritesJsonFile() {
        // Given
        Menu menu = new Menu();
        menu.setId("menu-json");
        menu.setMenuEntries(List.of());

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuExportDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(
                Path.of("export", DirectoryDefinitions.MENUS_DIR), "menu-json"))
                .thenReturn(menuExportDir);

        // When
        service.exportMenu(exportContext, menu);

        // Then
        verify(fileRepository).createDirIfRequired(menuExportDir);
        verify(jsonMapper).writeValue(
                eq(menuExportDir.resolve(MENU_EXCHANGE_FILENAME_JSON).toFile()),
                eq(menu)
        );
    }

    @Test
    void testExportMenuRecursivelyExportsNestedEntries() {
        // Given
        Menu leafMenu = new Menu();
        leafMenu.setId("leaf");
        leafMenu.setMenuEntries(List.of());
        leafMenu.setRestrictions(Set.of());

        Menu midMenu = new Menu();
        midMenu.setId("mid");
        midMenu.setMenuEntries(new ArrayList<>(List.of(leafMenu)));
        midMenu.setRestrictions(Set.of());

        Menu rootMenu = new Menu();
        rootMenu.setId("root");
        rootMenu.setMenuEntries(new ArrayList<>(List.of(midMenu)));

        ExportContext exportContext = ExportContext.builder()
                .exportDir(Path.of("export"))
                .exportConfiguration(ExportConfiguration.builder()
                        .applyRestrictions(false)
                        .build())
                .build();

        Path menuDir = Path.of("menu-dir");
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(menuDir);

        // When
        service.exportMenu(exportContext, rootMenu);

        // Then - root + mid + leaf = 3 JSON files written
        verify(jsonMapper, times(3)).writeValue(any(File.class), any(Menu.class));
        verify(fileRepository, times(3)).createDirIfRequired(menuDir);
    }
}
