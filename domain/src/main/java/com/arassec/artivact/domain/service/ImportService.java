package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Service for handling item import.
 */
@Slf4j
@Service
public class ImportService extends BaseFileService {

    /**
     * The service for account handling.
     */
    private final AccountService accountService;

    /**
     * The service for item handling.
     */
    private final ItemService itemService;

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Path to the project's items directory.
     */
    private final Path itemsFileDir;

    /**
     * Creates a new instance.
     *
     * @param accountService      The service for account handling.
     * @param itemService         The service for item handling.
     * @param projectDataProvider Provider for project data.
     * @param fileRepository      The application's {@link FileRepository}.
     * @param objectMapper        The object mapper.
     */
    public ImportService(AccountService accountService,
                         ItemService itemService,
                         ProjectDataProvider projectDataProvider,
                         FileRepository fileRepository,
                         ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.itemService = itemService;
        this.projectDataProvider = projectDataProvider;
        this.fileRepository = fileRepository;
        this.objectMapper = objectMapper;
        this.itemsFileDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR);
    }

    /**
     * Imports a previously exported item to the application by reading the export ZIP file.
     *
     * @param file     The export ZIP file.
     * @param apiToken The API token of the user to use for item import.
     */
    public void importItem(MultipartFile file, String apiToken) {
        if (StringUtils.hasText(apiToken)) {
            Optional<Account> accountOptional = accountService.loadByApiToken(apiToken);
            if (accountOptional.isEmpty()) {
                return;
            }
            Account account = accountOptional.get();
            if (Boolean.TRUE.equals(!account.getUser()) && Boolean.TRUE.equals(!account.getAdmin())) {
                return;
            }
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("import.zip");
        File importFile = projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.TEMP_DIR)
                .resolve(originalFilename)
                .toAbsolutePath()
                .toFile();

        try {
            file.transferTo(importFile);
        } catch (IOException e) {
            throw new ArtivactException("Could not save uploaded ZIP file!", e);
        }

        try (ZipFile zipFile = new ZipFile(importFile)) {
            ZipEntry itemJsonZipEntry = zipFile.getEntry("artivact.item.json");

            Item item = objectMapper.readValue(new String(zipFile.getInputStream(itemJsonZipEntry).readAllBytes()), Item.class);
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
    }

    /**
     * Imports items by scanning the project's items directory.
     */
    public void importItemsFromFilesystem() {
        log.info("Importing from directory: {}", itemsFileDir.toAbsolutePath());
        getItemPaths().forEach(itemPath -> {
            String itemId = itemPath.getFileName().toString();
            var item = itemService.loadTranslatedRestricted(itemId);
            if (item == null) {
                processNewItem(itemPath);
            } else {
                updateExistingItem(itemPath, item);
            }
        });
        log.info("Import finished.");
    }

    /**
     * Imports an item from the given path.
     *
     * @param itemPath The path to the item.
     */
    private void processNewItem(Path itemPath) {
        log.info("Creating item: {}", itemPath.getFileName());
        Item item = itemService.create();
        item.setId(itemPath.getFileName().toString());
        item.getMediaContent().setImages(getFiles(itemPath, ProjectDataProvider.IMAGES_DIR));
        item.getMediaContent().setModels(getFiles(itemPath, ProjectDataProvider.MODELS_DIR));
        itemService.save(item);
    }

    /**
     * Updates an existing item by reading the item's directory again.
     *
     * @param itemPath Path to the item's directory.
     * @param item     The item to update.
     */
    private void updateExistingItem(Path itemPath, Item item) {
        log.info("Updating item: {}", itemPath.getFileName());

        item.getMediaContent().getImages()
                .addAll(getFiles(itemPath, ProjectDataProvider.IMAGES_DIR));

        item.getMediaContent().setImages(item.getMediaContent().getImages().stream()
                .distinct()
                .toList()
        );

        item.getMediaContent().getModels()
                .addAll(getFiles(itemPath, ProjectDataProvider.MODELS_DIR));

        item.getMediaContent().setModels(item.getMediaContent().getModels().stream()
                .distinct()
                .toList()
        );

        itemService.save(item);
    }

    /**
     * Returns paths to every item in the project.
     *
     * @return The path to every item.
     */
    private List<Path> getItemPaths() {
        List<Path> result = new LinkedList<>();
        getItemPathsRecursively(itemsFileDir, result, 0);
        return result;
    }

    /**
     * Traverses the root directory to find items.
     *
     * @param root   The directory to start from.
     * @param target The target list to put result paths in.
     * @param depth  The current depth of the search.
     */
    private void getItemPathsRecursively(Path root, List<Path> target, int depth) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {
                if (depth == 2) {
                    log.debug("Found item directory: {}", path);
                    target.add(path);
                } else {
                    getItemPathsRecursively(path, target, depth + 1);
                }
            });
        } catch (IOException e) {
            throw new ArtivactException("Could not read item ids!", e);
        }
    }

}
