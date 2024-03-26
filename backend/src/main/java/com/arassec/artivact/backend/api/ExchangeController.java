package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.ExportService;
import com.arassec.artivact.backend.service.ImportService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDate;

/**
 * Controller for exchanging items and configurations between different application instances.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeController extends BaseController {

    /**
     * The application's {@link ConfigurationService}.
     */
    private final ConfigurationService configurationService;

    /**
     * The application's {@link ExportService}.
     */
    private final ExportService exportService;

    /**
     * The application's {@link ImportService}.
     */
    private final ImportService importService;

    /**
     * The object mapper for exports.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exports the current properties configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/properties/export")
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
     * Imports a properties configuration JSON file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/properties/import")
    public ResponseEntity<String> importPropertiesConfiguration(@RequestPart(value = "file") final MultipartFile file) {

        try {
            PropertiesConfiguration propertiesConfiguration = objectMapper.readValue(new String(file.getBytes()), PropertiesConfiguration.class);
            configurationService.savePropertiesConfiguration(propertiesConfiguration);
        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize properties configuration!", e);
        }

        return ResponseEntity.ok("Properties imported.");
    }

    /**
     * Exports the current tags configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/tags/export")
    public ResponseEntity<StreamingResponseBody> exportTagsConfiguration(HttpServletResponse response) {

        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();

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
     * Imports a tags configuration JSON file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/tags/import")
    public ResponseEntity<String> importTagsConfiguration(@RequestPart(value = "file") final MultipartFile file) {

        try {
            TagsConfiguration tagsConfiguration = objectMapper.readValue(new String(file.getBytes()), TagsConfiguration.class);
            configurationService.saveTagsConfiguration(tagsConfiguration);
        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }

        return ResponseEntity.ok("Tags imported.");
    }

    /**
     * Exports an item into a ZIP file containing the data as JSON file and all media assets.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @param itemId   The item's ID.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/item/{itemId}/export")
    public ResponseEntity<StreamingResponseBody> exportItem(HttpServletResponse response,
                                                            @PathVariable String itemId) {

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX + itemId + ".artivact.item.zip");
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(exportService.createItemExportFile(itemId));
    }

    /**
     * Called from the UI for local import of an item.
     *
     * @param file The item's export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/item/import")
    public ResponseEntity<String> importItem(@RequestPart(value = "file") final MultipartFile file) {
        importService.importItem(file, null);
        return ResponseEntity.ok("Item imported.");
    }

    /**
     * Imports item's by scanning the filesystem.
     *
     * @return A status string.
     */
    @PostMapping(value = "/item/import/filesystem")
    public ResponseEntity<String> importItems() {
        importService.importItemsFromFilesystem();
        return ResponseEntity.ok("scanned");
    }

    /**
     * Called by another server instance for remote import/sync!
     *
     * @param file     The item's export file to import.
     * @param apiToken The API token of the local user account used to import the item.
     * @return A status string.
     */
    @PostMapping(value = "sync/item/import/{apiToken}")
    public ResponseEntity<String> syncItem(@RequestPart(value = "file") final MultipartFile file,
                                           @PathVariable final String apiToken) {
        importService.importItem(file, apiToken);
        return ResponseEntity.ok("Item imported.");
    }

    /**
     * Called from the UI to start syncing an item with a remote application instance.
     *
     * @param itemId The item's ID.
     * @return The operation progress.
     */
    @PostMapping(value = "/item/{itemId}/sync-up")
    public ResponseEntity<OperationProgress> syncItemUp(@PathVariable String itemId) {
        return convert(exportService.syncItemUp(itemId));
    }

    /**
     * Returns the progress of a long-running operation of the {@link ExportService}.
     *
     * @return The current operation progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(exportService.getProgressMonitor());
    }

}
