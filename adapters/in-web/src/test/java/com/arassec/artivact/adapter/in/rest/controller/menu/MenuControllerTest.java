package com.arassec.artivact.adapter.in.rest.controller.menu;

import com.arassec.artivact.application.port.in.menu.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.menu.Menu;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link MenuController}.
 */
@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    /**
     * Controller under test.
     */
    @InjectMocks
    private MenuController menuController;

    /**
     * Use case for project directory handling.
     */
    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for menu loading.
     */
    @Mock
    private LoadMenuUseCase loadMenuUseCase;

    /**
     * Use case for menu saving.
     */
    @Mock
    private SaveMenuUseCase saveMenuUseCase;

    /**
     * Use case for deleting menus.
     */
    @Mock
    private DeleteMenuUseCase deleteMenuUseCase;

    /**
     * Use case to add a page to a menu.
     */
    @Mock
    private AddPageToMenuUseCase addPageToMenuUseCase;

    /**
     * Use case to export menus.
     */
    @Mock
    private ExportMenuUseCase exportMenuUseCase;

    /**
     * Use case to import menus.
     */
    @Mock
    private ImportMenuUseCase importMenuUseCase;

    /**
     * Repository for file handling.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Tests loading public menus.
     */
    @Test
    void testGetPublicMenus() {
        List<Menu> publicMenus = List.of(new Menu());
        when(loadMenuUseCase.loadTranslatedRestrictedMenus()).thenReturn(publicMenus);

        List<Menu> menus = menuController.getPublicMenus();

        assertThat(menus).isEqualTo(publicMenus);
    }

    /**
     * Tests saving a menu.
     */
    @Test
    void testSaveMenu() {
        Menu menuToSave = new Menu();
        List<Menu> publicMenus = List.of(new Menu());
        when(saveMenuUseCase.saveMenu(menuToSave)).thenReturn(publicMenus);

        ResponseEntity<List<Menu>> responseEntity = menuController.saveMenu(menuToSave);

        assertThat(responseEntity.getBody()).isEqualTo(publicMenus);
    }

    /**
     * Tests saving a list of menus.
     */
    @Test
    void testSaveAllMenus() {
        List<Menu> menusToSave = List.of(new Menu());
        List<Menu> publicMenus = List.of(new Menu());
        when(saveMenuUseCase.saveMenus(menusToSave)).thenReturn(publicMenus);

        ResponseEntity<List<Menu>> responseEntity = menuController.saveAllMenus(menusToSave);

        assertThat(responseEntity.getBody()).isEqualTo(publicMenus);
    }

    /**
     * Tests deleting a menu.
     */
    @Test
    void testDeleteMenu() {
        List<Menu> publicMenus = List.of(new Menu());
        when(deleteMenuUseCase.deleteMenu("menuId")).thenReturn(publicMenus);

        ResponseEntity<List<Menu>> responseEntity = menuController.deleteMenu("menuId");

        assertThat(responseEntity.getBody()).isEqualTo(publicMenus);
    }

    /**
     * Tests adding a page to a menu.
     */
    @Test
    void testAddPage() {
        List<Menu> publicMenus = List.of(new Menu());
        when(addPageToMenuUseCase.addPageToMenu("menuId")).thenReturn(publicMenus);

        ResponseEntity<List<Menu>> responseEntity = menuController.addPage("menuId");

        assertThat(responseEntity.getBody()).isEqualTo(publicMenus);
    }

    /**
     * Tests exporting a menu.
     */
    @Test
    @SneakyThrows
    void testExportMenu() {
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletResponse.getOutputStream()).thenReturn(outputStream);

        Path exportFilePath = Path.of("menu-export.zip");
        when(exportMenuUseCase.exportMenu("menuId")).thenReturn(exportFilePath);

        when(fileRepository.copy(exportFilePath, outputStream)).thenReturn(42L);

        ResponseEntity<StreamingResponseBody> responseEntity = menuController.exportMenu("menuId", httpServletResponse);

        assertThat(responseEntity.getBody()).isNotNull();
        responseEntity.getBody().writeTo(outputStream);

        verify(fileRepository).delete(exportFilePath);

        verify(httpServletResponse).setContentLength(42);
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(eq("Content-Disposition"), startsWith("attachment; filename="));
        verify(httpServletResponse).addHeader("Pragma", "no-cache");
        verify(httpServletResponse).addHeader("Expires", "0");
    }

    /**
     * Tests importing a menu.
     */
    @Test
    @SneakyThrows
    void testImportMenu() {
        Path tempFile = Path.of("target/upload_menu-export.zip");

        Files.deleteIfExists(tempFile);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("menu-export.zip");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("target"));

        ResponseEntity<String> responseEntity = menuController.importMenu(multipartFile);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEqualTo("Menu imported.");

        verify(importMenuUseCase).importMenu(tempFile);
        verify(fileRepository).delete(tempFile);
    }

}
