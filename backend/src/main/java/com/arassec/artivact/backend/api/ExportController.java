package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.ExportService;
import com.arassec.artivact.backend.service.exporter.model.ExportParams;
import com.arassec.artivact.backend.service.model.export.ContentExport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller for export management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/export")
public class ExportController extends BaseController {

    /**
     * The application's {@link ExportService}.
     */
    private final ExportService exportService;

    /**
     * Exports the pages of a menu and the sub-menu pages (if available).
     *
     * @param menuId The menu's ID.
     * @return The progress of the export.
     */
    @PostMapping("/content/{menuId}")
    public ResponseEntity<OperationProgress> exportContent(@PathVariable String menuId,
                                                           @RequestBody ExportParams params) {
        exportService.exportContent(params, menuId);
        return getProgress();
    }

    /**
     * Returns details about previous exports.
     *
     * @return List with details of existing exports.
     */
    @GetMapping("/content")
    public List<ContentExport> loadContentExports() {
        return exportService.loadContentExports();
    }

    /**
     * Exports the pages of a menu and the sub-menu pages (if available).
     *
     * @param menuId The menu's ID.
     * @return The progress of the export.
     */
    @DeleteMapping("/content/{menuId}")
    public ResponseEntity<OperationProgress> deleteContentExport(@PathVariable String menuId) {
        exportService.deleteContentExport(menuId);
        return getProgress();
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(exportService.getProgressMonitor());
    }

}
