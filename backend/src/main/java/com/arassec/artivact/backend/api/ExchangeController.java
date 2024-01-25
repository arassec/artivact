package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.ItemService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.model.item.MediaCreationContent;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ConfigurationService configurationService;

    private final ItemService itemService;

    private final ProjectRootProvider projectRootProvider;

    private final ObjectMapper exportObjectMapper;

    public ExchangeController(ConfigurationService configurationService,
                              ItemService itemService,
                              ProjectRootProvider projectRootProvider,
                              @Qualifier("exportObjectMapper") ObjectMapper exportObjectMapper) {
        this.configurationService = configurationService;
        this.itemService = itemService;
        this.projectRootProvider = projectRootProvider;
        this.exportObjectMapper = exportObjectMapper;
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

        Item item = itemService.loadUnrestricted(itemId);
        item.setMediaCreationContent(null);

        List<String> mediaFiles = itemService.getFilesForDownload(itemId);

        StreamingResponseBody streamResponseBody = out -> {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

            ZipEntry zipEntry = new ZipEntry("artivact.item.json");
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(exportObjectMapper.writeValueAsBytes(item));

            for (String mediaFile : mediaFiles) {
                File file = new File(mediaFile);
                zipEntry = new ZipEntry(file.getName());

                try (var inputStream = new FileInputStream(file)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                } catch (IOException e) {
                    log.error("Exception while reading and streaming data!", e);
                }

            }

            zipOutputStream.close();
        };

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + itemId + ".artivact.item.zip");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");

        return ResponseEntity.ok(streamResponseBody);
    }

    @PostMapping(value = "/item/import")
    public ResponseEntity<String> importItem(@RequestPart(value = "file") final MultipartFile file) {

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("import.zip");
        File importFile = projectRootProvider.getProjectRoot().resolve("temp").resolve(originalFilename).toAbsolutePath().toFile();

        try {
            file.transferTo(importFile);
        } catch (IOException e) {
            throw new ArtivactException("Could not save uploaded ZIP file!", e);
        }

        try (ZipFile zipFile = new ZipFile(importFile)) {
            ZipEntry itemJsonZipEntry = zipFile.getEntry("artivact.item.json");

            Item item = exportObjectMapper.readValue(new String(zipFile.getInputStream(itemJsonZipEntry).readAllBytes()), Item.class);
            item.setMediaCreationContent(new MediaCreationContent());

            item.getMediaContent().getImages().forEach(image -> {
                ZipEntry imageZipEntry = zipFile.getEntry(image);
                try {
                    itemService.saveImage(item.getId(), image, zipFile.getInputStream(imageZipEntry), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read image from imported ZIP file!", e);
                }
            });

            item.getMediaContent().getModels().forEach(model -> {
                ZipEntry modelZipEntry = zipFile.getEntry(model);
                try {
                    itemService.saveModel(item.getId(), model, zipFile.getInputStream(modelZipEntry), true);
                } catch (IOException e) {
                    throw new ArtivactException("Could not read model from imported ZIP file!", e);
                }
            });

            itemService.save(item);

        } catch (IOException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }

        return ResponseEntity.ok("Item imported.");
    }

}
