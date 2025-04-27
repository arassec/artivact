package com.arassec.artivact.domain.exchange.importer;

import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MenuImporter}.
 */
@ExtendWith(MockitoExtension.class)
class MenuImporterTest {

    /**
     * The importer under test.
     */
    @InjectMocks
    private MenuImporter menuImporter;

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
     * Service for item handling.
     */
    @Mock
    private PageImporter pageImporter;

    /**
     * Service for item handling.
     */
    @Mock
    private MenuService menuService;

    /**
     * Import context for testing.
     */
    private final ImportContext importContext = ImportContext.builder()
            .importDir(Path.of("import-dir"))
            .build();

    /**
     * Tests importing a menu without sub-menus.
     */
    @Test
    @SneakyThrows
    void testImportSingleMenu() {
        Menu menu = new Menu();
        menu.setId("menu-id");
        menu.setParentId("parent-id"); // Must be deleted during import!
        menu.setTargetPageId("target-page-id");

        when(fileRepository.read(Path.of("import-dir/menu-id.artivact.menu.json"))).thenReturn("menu-json");
        when(objectMapper.readValue("menu-json", Menu.class)).thenReturn(menu);

        menuImporter.importMenu(importContext, "menu-id", true);

        verify(pageImporter).importPage(importContext, "target-page-id", null);

        verify(menuService).saveMenu(menu);

        assertThat(menu.getParentId()).isNull();
    }

    /**
     * Tests importing a menu with menu entries.
     */
    @Test
    @SneakyThrows
    void testImportMenu() {
        Menu subMenu = new Menu();
        subMenu.setId("sub-menu-id");
        subMenu.setParentId("menu-id");
        subMenu.setTargetPageId("target-page-id");

        Menu menu = new Menu();
        menu.setId("menu-id");
        menu.setMenuEntries(List.of(subMenu));

        when(fileRepository.read(Path.of("import-dir/menu-id.artivact.menu.json"))).thenReturn("menu-json");
        when(objectMapper.readValue("menu-json", Menu.class)).thenReturn(menu);
        when(fileRepository.read(Path.of("import-dir/sub-menu-id.artivact.menu.json"))).thenReturn("sub-menu-json");
        when(objectMapper.readValue("sub-menu-json", Menu.class)).thenReturn(subMenu);

        menuImporter.importMenu(importContext, "menu-id", true);

        verify(pageImporter).importPage(importContext, "target-page-id", null);

        verify(menuService).saveMenu(menu);
    }

}
