package com.arassec.artivact.web.controller;

import com.arassec.artivact.domain.exchange.ExchangeProcessor;
import com.arassec.artivact.domain.service.ExportService;
import com.arassec.artivact.web.model.OperationProgress;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;

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
     * Returns details about available content exports.
     *
     * @return ZIP file containing the information about available content exports.
     */
    @GetMapping("/content/overview")
    public ResponseEntity<StreamingResponseBody> loadContentExportOverviews(HttpServletResponse response) {
        StreamingResponseBody streamResponseBody = exportService::loadContentExportOverviews;

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX + ExportService.CONTENT_EXPORT_OVERVIEWS_FILE);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Exports the current properties configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/properties")
    public ResponseEntity<StreamingResponseBody> exportPropertiesConfiguration(HttpServletResponse response) {

        String exportedPropertiesConfiguration = exportService.exportPropertiesConfiguration();

        StreamingResponseBody streamResponseBody = out -> {
            response.getOutputStream().write(exportedPropertiesConfiguration.getBytes());
            response.setContentLength(exportedPropertiesConfiguration.getBytes().length);
            exportService.cleanupPropertiesConfigurationExport();
        };

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + ExchangeProcessor.PROPERTIES_EXCHANGE_FILENAME_JSON);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Exports the current tags configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/tags")
    public ResponseEntity<StreamingResponseBody> exportTagsConfiguration(HttpServletResponse response) {
        String tagsConfigurationJson = exportService.exportTagsConfiguration();

        StreamingResponseBody streamResponseBody = out -> {
            response.getOutputStream().write(tagsConfigurationJson.getBytes());
            response.setContentLength(tagsConfigurationJson.getBytes().length);
            exportService.cleanupTagsConfigurationExport();
        };

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + ExchangeProcessor.TAGS_EXCHANGE_FILENAME_JSON);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Called from the UI to start syncing an item with a remote application instance.
     *
     * @param itemId The item's ID.
     * @return The operation progress.
     */
    @PostMapping(value = "/remote/item/{itemId}")
    public ResponseEntity<OperationProgress> exportItemToRemoteInstance(@PathVariable String itemId) {
        exportService.exportItemToRemoteInstance(itemId);
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
