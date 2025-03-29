package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.service.MenuService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ConfigurationController}.
 */
@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private MenuController controller;

    /**
     * Service mock.
     */
    @Mock
    private MenuService menuService;

    /**
     * Repository-Mock for file handling.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Tests getting application menus.
     */
    @Test
    void testPublicMenus() {
        List<Menu> menus = new LinkedList<>();
        when(menuService.loadTranslatedRestrictedMenus()).thenReturn(menus);
        assertEquals(menus, controller.getPublicMenus());
    }

    /**
     * Tests saving a menu.
     */
    @Test
    void testSaveMenu() {
        Menu menu = new Menu();
        List<Menu> allMenus = new LinkedList<>();
        when(menuService.saveMenu(menu)).thenReturn(allMenus);
        ResponseEntity<List<Menu>> responseEntity = controller.saveMenu(menu);
        assertEquals(allMenus, responseEntity.getBody());
    }

    /**
     * Tests saving all menus.
     */
    @Test
    void testSaveAllMenus() {
        List<Menu> allMenus = new LinkedList<>();
        when(menuService.saveMenus(allMenus)).thenReturn(allMenus);
        ResponseEntity<List<Menu>> responseEntity = controller.saveAllMenus(allMenus);
        assertEquals(allMenus, responseEntity.getBody());
    }

    /**
     * Tests deleting a menu.
     */
    @Test
    void testDeleteMenu() {
        controller.deleteMenu("abc123");
        verify(menuService, times(1)).deleteMenu("abc123");
    }

    /**
     * Tests adding a new page to a menu.
     */
    @Test
    void testAddPage() {
        controller.addPage("123abc");
        verify(menuService, times(1)).addPageToMenu("123abc");
    }

    /**
     * Tests exporting a menu.
     */
    @Test
    @SneakyThrows
    void testExportMenu() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        Path menuExport = mock(Path.class);
        when(menuService.exportMenu("menu-id")).thenReturn(menuExport);

        when(fileRepository.copy(eq(menuExport), any())).thenReturn(123L);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity = controller.exportMenu("menu-id", response);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setHeader(eq("Content-Disposition"), anyString());
        verify(response).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(response).addHeader(HttpHeaders.EXPIRES, "0");

        assertThat(streamingResponseBodyResponseEntity.getBody()).isNotNull();
        streamingResponseBodyResponseEntity.getBody().writeTo(response.getOutputStream());

        verify(response).setContentLength(123);

        verify(fileRepository).delete(menuExport);
    }
}
