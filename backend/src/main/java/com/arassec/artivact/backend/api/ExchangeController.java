package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.ExchangeService;
import com.arassec.artivact.backend.service.ItemService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/exchange")
public class ExchangeController extends BaseController {

    private final ConfigurationService configurationService;

    private final ItemService itemService;

    private final ObjectMapper exportObjectMapper;

    private final ExchangeService exchangeService;

    public ExchangeController(ConfigurationService configurationService,
                              ItemService itemService,
                              @Qualifier("exportObjectMapper") ObjectMapper exportObjectMapper,
                              ExchangeService exchangeService) {
        this.configurationService = configurationService;
        this.itemService = itemService;
        this.exportObjectMapper = exportObjectMapper;
        this.exchangeService = exchangeService;
    }

    @GetMapping(value = "/properties/export")
    public ResponseEntity<StreamingResponseBody> exportPropertiesConfiguration(HttpServletResponse response) {

        PropertiesConfiguration propertiesConfiguration = configurationService.loadPropertiesConfiguration();

        try {
            String propertiesConfigurationJson = exportObjectMapper.writeValueAsString(propertiesConfiguration);

            StreamingResponseBody streamResponseBody = out -> {
                response.getOutputStream().write(propertiesConfigurationJson.getBytes());
                response.setContentLength(propertiesConfigurationJson.getBytes().length);
            };

            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + LocalDate.now() + ".artivact.properties-configuration.json");
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Expires", "0");

            return ResponseEntity.ok(streamResponseBody);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not serialize properties configuration!", e);
        }
    }

    @PostMapping(value = "/properties/import")
    public ResponseEntity<String> importPropertiesConfiguration(@RequestPart(value = "file") final MultipartFile file) {

        try {
            PropertiesConfiguration propertiesConfiguration = exportObjectMapper.readValue(new String(file.getBytes()), PropertiesConfiguration.class);
            configurationService.savePropertiesConfiguration(propertiesConfiguration);
        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize properties configuration!", e);
        }

        return ResponseEntity.ok("Properties imported.");
    }

    @GetMapping(value = "/tags/export")
    public ResponseEntity<StreamingResponseBody> exportTagsConfiguration(HttpServletResponse response) {

        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();

        try {
            String tagsConfigurationJson = exportObjectMapper.writeValueAsString(tagsConfiguration);

            StreamingResponseBody streamResponseBody = out -> {
                response.getOutputStream().write(tagsConfigurationJson.getBytes());
                response.setContentLength(tagsConfigurationJson.getBytes().length);
            };

            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + LocalDate.now() + ".artivact.tags-configuration.json");
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Expires", "0");

            return ResponseEntity.ok(streamResponseBody);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not serialize tags configuration!", e);
        }
    }

    @PostMapping(value = "/tags/import")
    public ResponseEntity<String> importTagsConfiguration(@RequestPart(value = "file") final MultipartFile file) {

        try {
            TagsConfiguration tagsConfiguration = exportObjectMapper.readValue(new String(file.getBytes()), TagsConfiguration.class);
            configurationService.saveTagsConfiguration(tagsConfiguration);
        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }

        return ResponseEntity.ok("Tags imported.");
    }

    @GetMapping(value = "/item/{itemId}/export")
    public ResponseEntity<StreamingResponseBody> exportItem(HttpServletResponse response,
                                                            @PathVariable String itemId) {

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + itemId + ".artivact.item.zip");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");

        return ResponseEntity.ok(exchangeService.createItemExportFile(itemId));
    }

    /**
     * Called from the UI for local import.
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/item/import")
    public ResponseEntity<String> importItem(@RequestPart(value = "file") final MultipartFile file) {
        exchangeService.importItem(file, null);
        return ResponseEntity.ok("Item imported.");
    }

    /**
     * Called by another server instance for remote import/sync!
     *
     * @param file
     * @param apiToken
     * @return
     */
    @PostMapping(value = "sync/item/import/{apiToken}")
    public ResponseEntity<String> syncItem(@RequestPart(value = "file") final MultipartFile file,
                                           @PathVariable final String apiToken) {
        exchangeService.importItem(file, apiToken);
        return ResponseEntity.ok("Item imported.");
    }

    @PostMapping(value = "/item/{itemId}/sync-up")
    public ResponseEntity<OperationProgress> syncItemUp(@PathVariable String itemId) {
        return convert(exchangeService.syncItemUp(itemId));
    }

    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(exchangeService.getProgressMonitor());
    }

}
