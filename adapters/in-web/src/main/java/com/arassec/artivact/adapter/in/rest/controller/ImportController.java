package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.application.port.in.collection.ImportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.exchange.ImportContentUseCase;
import com.arassec.artivact.domain.exception.ArtivactException;
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

    private final ImportCollectionUseCase importCollectionUseCase;
    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;
    private final ImportTagsConfigurationUseCase importTagsConfigurationUseCase;
    private final ImportContentUseCase importContentUseCase;

    /**
     * Imports a properties configuration JSON file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/properties")
    public ResponseEntity<String> importPropertiesConfiguration(@RequestPart(value = "file") final MultipartFile file) {
        try {
            importPropertiesConfigurationUseCase.importPropertiesConfiguration(new String(file.getBytes()));
            return ResponseEntity.ok("Properties imported.");
        } catch (IOException e) {
            throw new ArtivactException("Could not read properties configuration file!", e);
        }
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
            importTagsConfigurationUseCase.importTagsConfiguration(new String(file.getBytes()));
            return ResponseEntity.ok("Tags imported.");
        } catch (IOException e) {
            throw new ArtivactException("Could not read tags configuration file!", e);
        }
    }

    /**
     * Imports a menu export ZIP file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/menu")
    public ResponseEntity<String> importMenu(@RequestPart(value = "file") final MultipartFile file) {
        importContentUseCase.importContent(convertToPath(file));
        return ResponseEntity.ok("Menu imported.");
    }

    /**
     * Imports an item's export ZIP file.
     *
     * @param file The export file to import.
     * @return A status string.
     */
    @PostMapping(value = "/item")
    public ResponseEntity<String> importItem(@RequestPart(value = "file") final MultipartFile file) {
        importContentUseCase.importContent(convertToPath(file));
        return ResponseEntity.ok("Item imported.");
    }

    /**
     * Called by another Artivact instance for remote import/sync!
     *
     * @param file     The item's export file to import.
     * @param apiToken The API token of the local user account used to import the item.
     * @return A status string.
     */
    @PostMapping(value = "/item/{apiToken}")
    public ResponseEntity<String> importItemWithApiToken(@RequestPart(value = "file") final MultipartFile file,
                                                         @PathVariable final String apiToken) {
        importContentUseCase.importContent(convertToPath(file), apiToken);
        return ResponseEntity.ok("Item synchronized.");
    }

    /**
     * Imports a collection export and its content into the application.
     *
     * @param file The collection export to import.
     */
    @PostMapping("/collection")
    public void importCollection(@RequestPart(value = "file") final MultipartFile file) {
        importCollectionUseCase.importCollection(convertToPath(file));
    }

    /**
     * Imports a collection export for distribution into the application.
     *
     * @param file The collection export to import.
     */
    @PostMapping("/collection/for-distribution")
    public void importCollectionForDistribution(@RequestPart(value = "file") final MultipartFile file) {
        importCollectionUseCase.importCollectionForDistribution(convertToPath(file));
    }

}
