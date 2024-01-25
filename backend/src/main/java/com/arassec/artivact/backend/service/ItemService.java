package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ItemEntityRepository;
import com.arassec.artivact.backend.persistence.model.ItemEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.model.item.MediaContent;
import com.arassec.artivact.backend.service.model.item.MediaCreationContent;
import com.arassec.artivact.backend.service.model.tag.Tag;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
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

@Slf4j
@Service
@Transactional
public class ItemService extends BaseFileService {

    private final ItemEntityRepository itemEntityRepository;

    private final ConfigurationService configurationService;

    private final SearchService searchService;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path itemsDir;

    public ItemService(ItemEntityRepository itemEntityRepository,
                       ConfigurationService configurationService,
                       SearchService searchService,
                       ObjectMapper objectMapper,
                       ProjectRootProvider projectRootProvider) {
        this.itemEntityRepository = itemEntityRepository;
        this.configurationService = configurationService;
        this.searchService = searchService;
        this.objectMapper = objectMapper;
        this.itemsDir = projectRootProvider.getProjectRoot().resolve(ITEMS_DIR);
        if (!Files.exists(itemsDir)) {
            try {
                Files.createDirectories(itemsDir);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create items directory!", e);
            }
        }
    }

    public Item create() {
        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();

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

    @RestrictResult
    @TranslateResult
    public Item load(String itemId) {
        Optional<ItemEntity> itemEntityOptional = itemEntityRepository.findById(itemId);
        if (itemEntityOptional.isPresent()) {
            TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();
            ItemEntity itemEntity = itemEntityOptional.get();
            Item item = fromJson(itemEntity.getContentJson(), Item.class);
            item.setVersion(itemEntity.getVersion());
            item.setTags(item.getTags().stream()
                    .map(tag -> tagsConfiguration.getTags().stream()
                            .filter(configuredTag -> tag.getId().equals(configuredTag.getId()))
                            .findFirst()
                            .orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            if (item.getMediaContent() == null) {
                item.setMediaContent(new MediaContent());
            }
            if (item.getMediaCreationContent() == null) {
                item.setMediaCreationContent(new MediaCreationContent());
            }

            return item;
        }

        return null;
    }

    @TranslateResult
    public Item loadUnrestricted(String itemId) {
        return load(itemId);
    }

    @GenerateIds
    public Item save(Item item) {
        ItemEntity itemEntity = itemEntityRepository.findById(item.getId()).orElse(new ItemEntity());

        itemEntity.setId(item.getId());
        itemEntity.setVersion(item.getVersion());
        itemEntity.setContentJson(toJson(item));

        getDanglingImages(item).forEach(imageToDelete -> {
            try {
                Files.deleteIfExists(getSubdirFilePath(itemsDir, item.getId(), IMAGES_DIR).resolve(imageToDelete));
                for (ImageSize imageSize : ImageSize.values()) {
                    Files.deleteIfExists(getSubdirFilePath(itemsDir, item.getId(), IMAGES_DIR)
                            .resolve(imageSize.name() + "-" + imageToDelete));
                }
            } catch (IOException e) {
                log.error("Could not delete obsolete image from filesystem!", e);
            }
        });

        List<String> modelsInItem = item.getMediaContent().getModels();

        List<String> modelsToDelete = getFiles(getDirFromId(itemsDir, item.getId()), MODELS_DIR);
        modelsToDelete.removeAll(modelsInItem);
        modelsToDelete.forEach(imageToDelete -> {
            try {
                Files.deleteIfExists(getSubdirFilePath(itemsDir, item.getId(), MODELS_DIR).resolve(imageToDelete));
            } catch (IOException e) {
                log.error("Could not delete obsolete model from filesystem!", e);
            }
        });

        ItemEntity savedItemEntity = itemEntityRepository.save(itemEntity);

        item.setVersion(savedItemEntity.getVersion());

        searchService.updateIndex(item);

        return item;
    }

    public List<String> getDanglingImages(Item item) {
        List<String> imagesInItem = new LinkedList<>(item.getMediaContent().getImages());
        item.getMediaCreationContent().getImageSets().forEach(creationImageSet -> imagesInItem.addAll(creationImageSet.getFiles()));

        List<String> allImagesInFolder = getFiles(getDirFromId(itemsDir, item.getId()), IMAGES_DIR);
        allImagesInFolder.removeAll(imagesInItem);

        return allImagesInFolder;
    }

    public void delete(String itemId) {
        itemEntityRepository.deleteById(itemId);
        deleteDirAndEmptyParents(getDirFromId(itemsDir, itemId));
    }

    public FileSystemResource loadImage(String itemId, String fileName, ImageSize targetSize) {
        return loadImage(itemsDir, itemId, fileName, targetSize, IMAGES_DIR);
    }


    public FileSystemResource loadModel(String itemId, String fileName) {
        return new FileSystemResource(itemsDir
                .resolve(itemId.substring(0, 3))
                .resolve(itemId.substring(3, 6))
                .resolve(itemId)
                .resolve(MODELS_DIR)
                .resolve(fileName));
    }

    public List<String> getFilesForDownload(String itemId) {
        List<String> result = new LinkedList<>();

        Item item = load(itemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + itemId + " found!");
        }

        result.addAll(item.getMediaContent().getImages().stream()
                .map(image -> itemsDir
                        .resolve(itemId.substring(0, 3))
                        .resolve(itemId.substring(3, 6))
                        .resolve(itemId)
                        .resolve(IMAGES_DIR)
                        .resolve(image)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        result.addAll(item.getMediaContent().getModels().stream()
                .map(model -> itemsDir
                        .resolve(itemId.substring(0, 3))
                        .resolve(itemId.substring(3, 6))
                        .resolve(itemId)
                        .resolve(MODELS_DIR)
                        .resolve(model)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        return result;
    }

    /**
     * @param itemId ID of the vault item.
     * @param file   The image file to save.
     */
    public void addImage(String itemId, MultipartFile file) {
        Item item = load(itemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + itemId + " found!");
        }

        try {
            item.getMediaContent().getImages().add(
                    saveFile(itemId, file.getOriginalFilename(), file.getInputStream(), IMAGES_DIR, null, false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add image!", e);
        }

        save(item);
    }

    /**
     * @param itemId ID of the vault item.
     * @param file   The model file to save.
     */
    public void addModel(String itemId, MultipartFile file) {
        Item item = load(itemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + itemId + " found!");
        }

        try {
            item.getMediaContent().getModels().add(
                    saveFile(itemId, file.getOriginalFilename(), file.getInputStream(), MODELS_DIR, "glb", false)
            );
        } catch (IOException e) {
            throw new ArtivactException("Could not add model!", e);
        }

        save(item);
    }

    public void saveImage(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        saveFile(itemId, filename, data, IMAGES_DIR, null, keepAssetNumber);
    }

    public void saveModel(String itemId, String filename, InputStream data, boolean keepAssetNumber) {
        saveFile(itemId, filename, data, MODELS_DIR, null, keepAssetNumber);
    }

    public void copyFile(String itemId, String filename, String subDir, Path targetDir) {
        Path sourceDir = getSubdirFilePath(itemsDir, itemId, subDir);
        try {
            Files.copy(sourceDir.resolve(filename).toAbsolutePath(), targetDir.resolve(filename).toAbsolutePath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy file!", e);
        }
    }

    private String saveFile(String itemId, String filename, InputStream data, String subDir, String requiredFileExtension, boolean keepAssetNumber) {
        Path targetDir = getSubdirFilePath(itemsDir, itemId, subDir);

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

        try {
            Files.copy(data, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save file!", e);
        }

        return assetName;
    }

    private int getNextAssetNumber(Path assetDir) {
        var highestNumber = 0;
        if (!Files.exists(assetDir)) {
            try {
                Files.createDirectories(assetDir);
            } catch (IOException e) {
                throw new ArtivactException("Could not create asset directory!", e);
            }
        }
        try (Stream<Path> stream = Files.list(assetDir)) {
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
        } catch (IOException e) {
            throw new ArtivactException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private Optional<String> getFilenameWithoutExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(0, filename.indexOf(".")));
    }

    private String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

}
