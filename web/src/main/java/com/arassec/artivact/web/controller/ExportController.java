package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.export.ContentExport;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.ExportService;
import com.arassec.artivact.web.model.OperationProgress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
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
     * The application's {@link ConfigurationService}.
     */
    private final ConfigurationService configurationService;

    /**
     * The object mapper for exports.
     */
    private final ObjectMapper objectMapper;

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
     * Returns the selected content export file.
     *
     * @return The content export.
     */
    @GetMapping("/content/{menuId}/{exportType}")
    public ResponseEntity<StreamingResponseBody> downloadContentExport(HttpServletResponse response,
                                                                       @PathVariable String menuId,
                                                                       @PathVariable ExportType exportType) {

        String exportFilename = exportService.getContentExportFile(menuId, exportType).getFileName().toString();

        StreamingResponseBody streamResponseBody = out -> exportService.loadContentExport(menuId, exportType, out);

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX + exportFilename);

        return ResponseEntity.ok(streamResponseBody);
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
     * Exports the current properties configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/properties")
    public ResponseEntity<StreamingResponseBody> exportPropertiesConfiguration(HttpServletResponse response) {

        PropertiesConfiguration propertiesConfiguration = configurationService.loadPropertiesConfiguration();

        try {
            String propertiesConfigurationJson = objectMapper.writeValueAsString(propertiesConfiguration);

            StreamingResponseBody streamResponseBody = out -> {
                response.getOutputStream().write(propertiesConfigurationJson.getBytes());
                response.setContentLength(propertiesConfigurationJson.getBytes().length);
            };

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                    + LocalDate.now() + ".artivact.properties-configuration.json");
            response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
            response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

            return ResponseEntity.ok(streamResponseBody);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not serialize properties configuration!", e);
        }
    }

    /**
     * Exports the current tags configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/tags")
    public ResponseEntity<StreamingResponseBody> exportTagsConfiguration(HttpServletResponse response) {

        TagsConfiguration tagsConfiguration = configurationService.loadTranslatedRestrictedTags();

        try {
            String tagsConfigurationJson = objectMapper.writeValueAsString(tagsConfiguration);

            StreamingResponseBody streamResponseBody = out -> {
                response.getOutputStream().write(tagsConfigurationJson.getBytes());
                response.setContentLength(tagsConfigurationJson.getBytes().length);
            };

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                    + LocalDate.now() + ".artivact.tags-configuration.json");
            response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
            response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

            return ResponseEntity.ok(streamResponseBody);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not serialize tags configuration!", e);
        }
    }

    /**
     * Exports an item into a ZIP file containing the data as JSON file and all media assets.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @param itemId   The item's ID.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/item/{itemId}")
    public ResponseEntity<StreamingResponseBody> exportItem(HttpServletResponse response,
                                                            @PathVariable String itemId) {

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX + itemId + ".artivact.item.zip");
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(outputStream -> exportService.createItemExportFile(itemId, outputStream));
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
     * Called from the UI to start syncing all items with a remote application instance.
     *
     * @return The operation progress.
     */
    @PostMapping(value = "/remote/item")
    public ResponseEntity<OperationProgress> exportItemsToRemoteInstance() {
        exportService.exportItemsToRemoteInstance();
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
