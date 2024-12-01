package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.domain.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST-Controller for menu management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    /**
     * The application's {@link MenuService}.
     */
    private final MenuService menuService;


    /**
     * Returns the application's menu as configured by the user.
     *
     * @return The menu.
     */
    @GetMapping()
    public List<Menu> getPublicMenus() {
        return menuService.loadTranslatedRestrictedMenus();
    }

    /**
     * Saves a menu.
     *
     * @param menu The menu to save.
     * @return The list of all menus of the application.
     */
    @PostMapping()
    public ResponseEntity<List<Menu>> saveMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.saveMenu(menu));
    }

    /**
     * Saves all menus.
     *
     * @param menus The menus to save.
     * @return The list of all menus of the application.
     */
    @PostMapping("/all")
    public ResponseEntity<List<Menu>> saveAllMenus(@RequestBody List<Menu> menus) {
        return ResponseEntity.ok(menuService.saveMenus(menus));
    }

    /**
     * Deletes a single menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<List<Menu>> deleteMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(menuService.deleteMenu(menuId));
    }

    /**
     * Adds a page to a menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @PostMapping("/{menuId}/page")
    public ResponseEntity<List<Menu>> addPage(@PathVariable String menuId) {
        return ResponseEntity.ok(menuService.addPageToMenu(menuId));
    }

    /**
     * Saves the cover picture of a menu.
     *
     * @param menuId The menu's ID.
     * @param file   The uploaded cover-image.
     */
    @PostMapping("/{menuId}/cover-picture")
    public void saveMenuCoverImage(@PathVariable String menuId,
                                   @RequestPart(value = "file") final MultipartFile file) {
        synchronized (this) {
            try {
                menuService.saveMenuCoverPicture(menuId, file.getOriginalFilename(), file.getInputStream());
            } catch (IOException e) {
                throw new ArtivactException("Could not save uploaded cover image!", e);
            }
        }
    }

}
