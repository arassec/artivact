package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.ImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for exchanging items and configurations between different application instances.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import")
public class ImportController extends BaseController {

    /**
     * The application's {@link ConfigurationService}.
     */
    private final ConfigurationService configurationService;

    /**
     * The application's {@link ImportService}.
     */
    private final ImportService importService;

    /**
     * The object mapper for exports.
     */
    private final ObjectMapper objectMapper;

    /**
     * Imports a properties configuration JSON file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/properties")
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
     * Imports a tags configuration JSON file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/tags")
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
     * Called from the UI for local import of an item.
     *
     * @param file The item's export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/item")
    public ResponseEntity<String> importItem(@RequestPart(value = "file") final MultipartFile file) {
        importService.importItem(file, null);
        return ResponseEntity.ok("Item imported.");
    }

    /**
     * Imports item's by scanning the filesystem.
     *
     * @return A status string.
     */
    @PostMapping(value = "/item/filesystem")
    public ResponseEntity<String> importItems() {
        importService.importItemsFromFilesystem();
        return ResponseEntity.ok("Items scanned.");
    }

    /**
     * Called by another server instance for remote import/sync!
     *
     * @param file     The item's export file to import.
     * @param apiToken The API token of the local user account used to import the item.
     * @return A status string.
     */
    @PostMapping(value = "/remote/item/{apiToken}")
    public ResponseEntity<String> syncItem(@RequestPart(value = "file") final MultipartFile file,
                                           @PathVariable final String apiToken) {
        importService.importItem(file, apiToken);
        return ResponseEntity.ok("Item synchronized.");
    }

}
