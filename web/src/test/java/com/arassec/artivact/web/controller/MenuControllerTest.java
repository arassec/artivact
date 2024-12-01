package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.domain.service.MenuService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
     * Tests saving a content export cover image.
     */
    @Test
    @SneakyThrows
    void testSaveMenuCoverImage() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("content-export.zip");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
        when(multipartFile.getInputStream()).thenReturn(byteArrayInputStream);

        controller.saveMenuCoverImage("menu-id", multipartFile);

        verify(menuService).saveMenuCoverPicture("menu-id", "content-export.zip", byteArrayInputStream);
    }

    /**
     * Tests saving a content export cover image.
     */
    @Test
    @SneakyThrows
    void testSaveMenuCoverImageFailsafe() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("test-exception"));

        assertThrows(ArtivactException.class, () -> controller.saveMenuCoverImage("menu-id", multipartFile));
    }

}
