package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ImportService extends BaseFileService {

    private static final String CREATOR_DATA_FILE = "data.json";

    private static final String CREATOR_DATA_NOTES = "notes";

    private final Path itemsFileDir;

    private final ItemService itemService;

    @Getter
    private final ObjectMapper objectMapper;

    public ImportService(final ItemService itemService,
                         final ObjectMapper objectMapper,
                         ProjectRootProvider projectRootProvider) {
        this.itemService = itemService;
        this.objectMapper = objectMapper;
        this.itemsFileDir = projectRootProvider.getProjectRoot().resolve(ITEMS_FILE_DIR);
    }

    public void importCreatorItems() {
        log.info("Importing from directory: {}", itemsFileDir.toAbsolutePath());
        getItemIdsFromCreatorExport().forEach(itemPath -> {
            String itemId = itemPath.getFileName().toString();
            var vaultItem = itemService.load(itemId);
            if (vaultItem == null) {
                processNewEntity(itemPath);
            } else {
                updateExistingEntity(itemPath, vaultItem);
            }
        });
        log.info("Import finished.");
    }

    private void processNewEntity(Path itemPath) {
        log.info("Creating item: {}", itemPath.getFileName());
        Item item = itemService.create();
        item.getDescription().setValue(readNotes(itemPath));
        item.setId(itemPath.getFileName().toString());
        item.getMediaContent().setImages(getFiles(itemPath, IMAGES_DIR));
        item.getMediaContent().setModels(getFiles(itemPath, MODELS_DIR));
        itemService.save(item);
    }

    private void updateExistingEntity(Path itemPath, Item item) {
        log.info("Updating item: {}", itemPath.getFileName());

        item.getMediaContent().getImages()
                .addAll(getFiles(itemPath, IMAGES_DIR));

        item.getMediaContent().setImages(item.getMediaContent().getImages().stream()
                .distinct()
                .toList()
        );

        item.getMediaContent().getModels()
                .addAll(getFiles(itemPath, MODELS_DIR));

        item.getMediaContent().setModels(item.getMediaContent().getModels().stream()
                .distinct()
                .toList()
        );

        itemService.save(item);
    }

    private List<Path> getItemIdsFromCreatorExport() {
        List<Path> result = new LinkedList<>();
        findArtivactIdsRecursively(itemsFileDir, result);
        return result;
    }

    private void findArtivactIdsRecursively(Path root, List<Path> target) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {
                if (path.getFileName().toString().equals(CREATOR_DATA_FILE)) {
                    target.add(path.getParent());
                } else if (Files.isDirectory(path)) {
                    findArtivactIdsRecursively(path, target);
                }
            });
        } catch (IOException e) {
            throw new ArtivactException("Could not read item ids!", e);
        }
    }

    private String readNotes(Path itemPath) {
        try {
            Map<String, Object> dataJson = getObjectMapper().readValue(
                    Files.readString(itemPath.resolve(CREATOR_DATA_FILE)), new TypeReference<>() {});
            if (dataJson.containsKey(CREATOR_DATA_NOTES)) {
                return String.valueOf(dataJson.get(CREATOR_DATA_NOTES));
            }
        } catch (IOException e) {
            log.warn("Could not read creator exported notes!", e);
        }
        return null;
    }

}
