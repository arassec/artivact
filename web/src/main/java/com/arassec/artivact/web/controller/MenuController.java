package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.ExchangeProcessor;
import com.arassec.artivact.domain.service.ExportService;
import com.arassec.artivact.domain.service.ImportService;
import com.arassec.artivact.domain.service.MenuService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * REST-Controller for menu management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menu")
public class MenuController extends BaseController {

    /**
     * The application's {@link MenuService}.
     */
    private final MenuService menuService;

    /**
     * The export service.
     */
    private final ExportService exportService;

    /**
     * The import service.
     */
    private final ImportService importService;

    /**
     * Repository for file handling.
     */
    private final FileRepository fileRepository;

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

    /**
     * Exports the menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @GetMapping("/{menuId}/export")
    public ResponseEntity<StreamingResponseBody> exportMenu(@PathVariable String menuId, HttpServletResponse response) {
        Path menuExport = exportService.exportMenu(menuId);

        StreamingResponseBody streamResponseBody = out -> {
            long bytesWritten = fileRepository.copy(menuExport, response.getOutputStream());
            response.setContentLength(Math.toIntExact(bytesWritten));
            exportService.cleanupPropertiesConfigurationExport();
            fileRepository.delete(menuExport);
        };

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + menuId + "." + ExchangeProcessor.MENU_EXCHANGE_FILENAME_ZIP);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Imports a menu export ZIP file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/import")
    public ResponseEntity<String> importMenu(@RequestPart(value = "file") final MultipartFile file) {
        importService.importDirectly(file);
        return ResponseEntity.ok("Menu imported.");
    }

}
