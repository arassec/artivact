package com.arassec.artivact.adapter.in.rest.controller.menu;

import com.arassec.artivact.adapter.in.rest.controller.BaseImportController;
import com.arassec.artivact.application.port.in.menu.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
public class MenuController extends BaseImportController {

    /**
     * Use case for project directory handling.
     */
    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Repository for file handling.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Use case for menu loading.
     */
    private final LoadMenuUseCase loadMenuUseCase;

    /**
     * Use case for menu saving.
     */
    private final SaveMenuUseCase saveMenuUseCase;

    /**
     * Use case for deleting menus.
     */
    private final DeleteMenuUseCase deleteMenuUseCase;

    /**
     * Use case to add a page to a menu.
     */
    private final AddPageToMenuUseCase addPageToMenuUseCase;

    /**
     * Use case to export menus.
     */
    private final ExportMenuUseCase exportMenuUseCase;

    /**
     * Use case to import menus.
     */
    private final ImportMenuUseCase importMenuUseCase;

    /**
     * Use case to relocate menus.
     */
    private final RelocateMenuUseCase relocateMenuUseCase;

    /**
     * Returns the application's menu as configured by the user.
     *
     * @return The menu.
     */
    @GetMapping()
    public List<Menu> getPublicMenus() {
        return loadMenuUseCase.loadTranslatedRestrictedMenus();
    }

    /**
     * Saves a menu.
     *
     * @param menu The menu to save.
     * @return The list of all menus of the application.
     */
    @PostMapping()
    public ResponseEntity<List<Menu>> saveMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(saveMenuUseCase.saveMenu(menu));
    }

    /**
     * Saves all menus.
     *
     * @param menus The menus to save.
     * @return The list of all menus of the application.
     */
    @PostMapping("/all")
    public ResponseEntity<List<Menu>> saveAllMenus(@RequestBody List<Menu> menus) {
        return ResponseEntity.ok(saveMenuUseCase.saveMenus(menus));
    }

    /**
     * Deletes a single menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<List<Menu>> deleteMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(deleteMenuUseCase.deleteMenu(menuId));
    }

    /**
     * Adds a page to a menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @PostMapping("/{menuId}/page")
    public ResponseEntity<List<Menu>> addPage(@PathVariable String menuId) {
        return ResponseEntity.ok(addPageToMenuUseCase.addPageToMenu(menuId));
    }

    /**
     * Exports the menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @GetMapping("/{menuId}/export")
    public ResponseEntity<StreamingResponseBody> exportMenu(@PathVariable String menuId, HttpServletResponse response) {
        Path menuExport = exportMenuUseCase.exportMenu(menuId);

        StreamingResponseBody streamResponseBody = out -> {
            long bytesWritten = fileRepository.copy(menuExport, response.getOutputStream());
            response.setContentLength(Math.toIntExact(bytesWritten));
            fileRepository.delete(menuExport);
        };

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + menuId + "." + ExchangeDefinitions.MENU_EXCHANGE_FILENAME_ZIP);
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
        Path tempFile = saveTempFile(file);
        importMenuUseCase.importMenu(tempFile);
        fileRepository.delete(tempFile);
        return ResponseEntity.ok("Menu imported.");
    }

    /**
     * Relocates the given menu to the target menu.
     *
     * @param menuId       The menu to relocate.
     * @param targetMenuId The target menu to move it to.
     * @return A status string.
     */
    @PutMapping(value = "/{menuId}/relocate/{targetMenuId}")
    public ResponseEntity<String> relocateMenu(@PathVariable String menuId, @PathVariable(required = false) String targetMenuId) {
        relocateMenuUseCase.relocateMenu(menuId, targetMenuId);
        return ResponseEntity.ok("Menu relocated.");
    }

}
