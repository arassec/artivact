package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaContent;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.ItemRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for item handling.
 */
@Slf4j
@Service
@Transactional
public class ItemService extends BaseFileService {

    /**
     * Error message prefix if no item was found.
     */
    private static final String NO_ITEM_ERROR_PREFIX = "No item found with ID: ";

    /**
     * Repository for items.
     */
    private final ItemRepository itemRepository;

    /**
     * Service for configuration handling.
     */
    private final ConfigurationService configurationService;

    /**
     * Service for search management.
     */
    private final SearchService searchService;

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
     * The directory containing the project's items.
     */
    private final Path itemsDir;

    /**
     * Creates a new instance.
     *
     * @param itemRepository       Repository for items.
     * @param configurationService Service for configuration handling.
     * @param searchService        Service for search management.
     * @param fileRepository       The application's {@link FileRepository}.
     * @param objectMapper         The object mapper.
     * @param projectDataProvider  The directory containing the project's items.
     */
    public ItemService(ItemRepository itemRepository,
                       ConfigurationService configurationService,
                       SearchService searchService,
                       FileRepository fileRepository,
                       ObjectMapper objectMapper,
                       ProjectDataProvider projectDataProvider) {

        this.itemRepository = itemRepository;
        this.configurationService = configurationService;
        this.searchService = searchService;
        this.fileRepository = fileRepository;
        this.objectMapper = objectMapper;
        this.itemsDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR);

        fileRepository.createDirIfRequired(itemsDir);
    }

    /**
     * Creates a new item.
     *
     * @return The newly created item.
     */
    public Item create() {
        TagsConfiguration tagsConfiguration = configurationService.loadTranslatedRestrictedTags();

        List<Tag> defaultTags = tagsConfiguration.getTags().stream()
                .filter(Tag::isDefaultTag)
                .toList();

        Item item = new Item();
        item.setVersion(0);
        item.setRestrictions(Set.of(Roles.ROLE_USER));
        item.setTitle(new TranslatableString());
        item.setDescription(new TranslatableString());
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());
        item.setTags(defaultTags);

        return item;
    }

    /**
     * Loads an item without regard to restrictions but translations.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    @SuppressWarnings("java:S6204") // Collection needs to be modifiable...
    public Optional<Item> load(String itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();
            item.setTags(item.getTags().stream()
                    .map(tag -> tagsConfiguration.getTags().stream()
                            .filter(configuredTag -> tag.getId().equals(configuredTag.getId()))
                            .findFirst()
                            .orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            // Create an empty TranslatableString value for every property:
            configurationService.loadPropertiesConfiguration().getCategories().forEach(propertyCategory ->
                    propertyCategory.getProperties().forEach(property -> {
                        if (!item.getProperties().containsKey(property.getId())) {
                            item.getProperties().put(property.getId(), new TranslatableString());
                        }
                    }));

            return Optional.of(item);
        }

        return Optional.empty();
    }

    /**
     * Loads an item without regard to restrictions but translations.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    @TranslateResult
    public Item loadTranslated(String itemId) {
        return load(itemId).orElseThrow();
    }

    /**
     * Loads the item with the given ID.
     *
     * @param itemId The item's ID.
     * @return The item.
     */
    @TranslateResult
    @RestrictResult
    public Item loadTranslatedRestricted(String itemId) {
        return load(itemId).orElseThrow();
    }

    /**
     * Saves an item.
     *
     * @param item The item to save.
     * @return The updated item.
     */
    @GenerateIds
    public Item save(Item item) {
        String itemId = item.getId();

        getDanglingImages(item).forEach(imageToDelete -> {
            fileRepository.delete(fileRepository.getSubdirFilePath(itemsDir, itemId, ProjectDataProvider.IMAGES_DIR).resolve(imageToDelete));
            for (ImageSize imageSize : ImageSize.values()) {
                fileRepository.delete(fileRepository.getSubdirFilePath(itemsDir, itemId, ProjectDataProvider.IMAGES_DIR)
                        .resolve(imageSize.name() + "-" + imageToDelete));
            }
        });

        List<String> modelsInItem = item.getMediaContent().getModels();

        List<String> modelsToDelete = getFiles(fileRepository.getDirFromId(itemsDir, itemId), ProjectDataProvider.MODELS_DIR);
        modelsToDelete.removeAll(modelsInItem);
        modelsToDelete.forEach(imageToDelete ->
                fileRepository.delete(fileRepository.getSubdirFilePath(itemsDir, itemId, ProjectDataProvider.MODELS_DIR).resolve(imageToDelete))
        );

        item = itemRepository.save(item);

        searchService.updateIndex(item);

        return item;
    }

    /**
     * Returns a list of images of an item that are not referenced by the item itself.
     *
     * @param item The item.
     * @return List of unreferenced images.
     */
    public List<String> getDanglingImages(Item item) {
        List<String> imagesInItem = new LinkedList<>(item.getMediaContent().getImages());
        item.getMediaCreationContent().getImageSets().forEach(creationImageSet -> imagesInItem.addAll(creationImageSet.getFiles()));

        List<String> allImagesInFolder = getFiles(fileRepository.getDirFromId(itemsDir, item.getId()), ProjectDataProvider.IMAGES_DIR);
        allImagesInFolder.removeAll(imagesInItem);

        return allImagesInFolder;
    }

    /**
     * Deletes an item.
     *
     * @param itemId The ID of the item to delete.
     */
    public void delete(String itemId) {
        itemRepository.deleteById(itemId);
        deleteDirAndEmptyParents(fileRepository.getDirFromId(itemsDir, itemId));
    }

    /**
     * Loads an item's image.
     *
     * @param itemId     The ID of the item.
     * @param filename   The filename of the image.
     * @param targetSize The desired image target size.
     * @return The (scaled) image as {@link FileSystemResource}.
     */
    public byte[] loadImage(String itemId, String filename, ImageSize targetSize) {
        FileSystemResource fileSystemResource = loadImage(itemsDir, itemId, filename, targetSize, ProjectDataProvider.IMAGES_DIR);
        try {
            return Files.readAllBytes(fileSystemResource.getFile().toPath());
        } catch (IOException e) {
            throw new ArtivactException("Could not load image!", e);
        }
    }

    /**
     * Loads an item's model.
     *
     * @param itemId   The ID of the item.
     * @param filename The model's filename.
     * @return The model as {@link FileSystemResource}.
     */
    public byte[] loadModel(String itemId, String filename) {
        FileSystemResource fileSystemResource = new FileSystemResource(itemsDir
                .resolve(itemId.substring(0, 3))
                .resolve(itemId.substring(3, 6))
                .resolve(itemId)
                .resolve(ProjectDataProvider.MODELS_DIR)
                .resolve(filename));
        try {
            return Files.readAllBytes(fileSystemResource.getFile().toPath());
        } catch (IOException e) {
            throw new ArtivactException("Could not load model!", e);
        }
    }

    /**
     * Returns the absolute paths to all media files of an item as Strings.
     *
     * @param itemId The item's ID.
     * @return List of media files of the item.
     */
    public List<String> getMediaFiles(String itemId) {
        List<String> result = new LinkedList<>();

        Item item = loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new ArtivactException(NO_ITEM_ERROR_PREFIX + itemId);
        }

        result.addAll(item.getMediaContent().getImages().stream()
                .map(image -> itemsDir
                        .resolve(itemId.substring(0, 3))
                        .resolve(itemId.substring(3, 6))
                        .resolve(itemId)
                        .resolve(ProjectDataProvider.IMAGES_DIR)
                        .resolve(image)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        result.addAll(item.getMediaContent().getModels().stream()
                .map(model -> itemsDir
                        .resolve(itemId.substring(0, 3))
                        .resolve(itemId.substring(3, 6))
                        .resolve(itemId)
                        .resolve(ProjectDataProvider.MODELS_DIR)
                        .resolve(model)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        return result;
    }

    /**
     * Adds an image to the item.
     *
     * @param itemId ID of the item.
     * @param file   The image file to add.
     */
    public void addImage(String itemId, MultipartFile file) {
        Item item = loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new ArtivactException(NO_ITEM_ERROR_PREFIX + itemId);
        }

        try {
            item.getMediaContent().getImages().add(
                    saveFile(itemId, file.getOriginalFilename(), file.getInputStream(), ProjectDataProvider.IMAGES_DIR, null, false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add image!", e);
        }

        save(item);
    }

    /**
     * Adds a model to the item.
     *
     * @param itemId ID of the item.
     * @param file   The model file to add.
     */
    public void addModel(String itemId, MultipartFile file) {
        Item item = loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new ArtivactException(NO_ITEM_ERROR_PREFIX + itemId);
        }

        try {
            item.getMediaContent().getModels().add(
                    saveFile(itemId, file.getOriginalFilename(), file.getInputStream(), ProjectDataProvider.MODELS_DIR, "glb", false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add model!", e);
        }

        save(item);
    }

    /**
     * Saves an image to an item.
     *
     * @param itemId          The ID of the item.
     * @param filename        The name of the image file.
     * @param data            The image's data as {@link InputStream}.
     * @param keepAssetNumber Set to {@code true}, if the asset number from the image's filename should be used.
     */
    public void saveImage(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        saveFile(itemId, filename, data, ProjectDataProvider.IMAGES_DIR, null, keepAssetNumber);
    }

    /**
     * Saves a model to an item.
     *
     * @param itemId          The ID of the item.
     * @param filename        The name of the model file.
     * @param data            The model's data as {@link InputStream}.
     * @param keepAssetNumber Set to {@code true}, if the asset number from the model's filename should be used.
     */
    public void saveModel(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        saveFile(itemId, filename, data, ProjectDataProvider.MODELS_DIR, null, keepAssetNumber);
    }

    /**
     * Returns the IDs of items that have to be updated on the remote instance.
     *
     * @return List of item IDs.
     */
    public List<String> getItemIdsForRemoteSync() {
        return itemRepository.findItemIdsForRemoteExport();
    }

    /**
     * Saves a file to the item and provides it with a new asset number (if required).
     *
     * @param itemId                The item's ID.
     * @param filename              The file to save.
     * @param data                  The file's data as {@link InputStream}.
     * @param subDir                The subdirectory inside the item's directory to save the file to.
     * @param requiredFileExtension Provide to restrict the allowed file's extension.
     * @param keepAssetNumber       Set to {@code true}, if the asset number from the model's filename should be used.
     * @return The new file name.
     */
    private String saveFile(String itemId, String filename, InputStream data, String subDir, String requiredFileExtension, boolean keepAssetNumber) {
        Path targetDir = fileRepository.getSubdirFilePath(itemsDir, itemId, subDir);

        int assetNumber = getNextAssetNumber(targetDir);
        if (keepAssetNumber) {
            assetNumber = Integer.parseInt(getFilenameWithoutExtension(filename).orElseThrow());
        }
        String fileExtension = getExtension(filename).orElseThrow();

        if (StringUtils.hasText(requiredFileExtension) && !requiredFileExtension.equals(fileExtension)) {
            throw new ArtivactException("Unsupported file format. Files must be in '" + requiredFileExtension + "' format!");
        }

        String assetName = getAssetName(assetNumber, fileExtension);

        Path targetPath = targetDir.resolve(assetName);

        fileRepository.copy(data, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return assetName;
    }

    /**
     * Returns the next available asset number.
     *
     * @param assetDir The directory of existing assets.
     * @return The next, free asset number.
     */
    private int getNextAssetNumber(Path assetDir) {
        var highestNumber = 0;
        fileRepository.createDirIfRequired(assetDir);
        try (Stream<Path> stream = fileRepository.list(assetDir)) {
            List<Path> assets = stream.toList();
            for (Path path : assets) {
                if (".".equals(path.getFileName().toString()) || "..".equals(path.getFileName().toString())) {
                    continue;
                }
                String fileName = path.getFileName().toString();

                for (ImageSize imageSize : ImageSize.values()) {
                    if (fileName.startsWith(imageSize.name())) {
                        fileName = fileName.replace(imageSize.name() + "-", "");
                    }
                }

                var number = Integer.parseInt(fileName.split("\\.")[0]);
                if (number > highestNumber) {
                    highestNumber = number;
                }
            }
        }
        return (highestNumber + 1);
    }

    /**
     * Returns the file extension of the given filename.
     *
     * @param filename The name of the file.
     * @return The file extension.
     */
    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * Returns the raw filename without file extension.
     *
     * @param filename The complete name of the file.
     * @return The name without extension.
     */
    private Optional<String> getFilenameWithoutExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(0, filename.indexOf(".")));
    }

    /**
     * Formats the given asset number and extension into a filename.
     *
     * @param assetNumber The asset number.
     * @param extension   The file's extension.
     * @return The filename.
     */
    private String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

}
