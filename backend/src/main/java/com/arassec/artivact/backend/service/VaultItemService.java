package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ItemEntityRepository;
import com.arassec.artivact.backend.persistence.model.ItemEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.exception.VaultException;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.backend.service.model.tag.Tag;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.model.item.MediaContent;
import com.arassec.artivact.backend.service.model.item.Item;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class VaultItemService extends BaseFileService {

    private final ItemEntityRepository itemEntityRepository;

    private final ConfigurationService configurationService;

    private final SearchService searchService;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path itemsFileDir;

    public VaultItemService(ItemEntityRepository itemEntityRepository,
                            ConfigurationService configurationService,
                            SearchService searchService,
                            ObjectMapper objectMapper,
                            ProjectRootProvider projectRootProvider) {
        this.itemEntityRepository = itemEntityRepository;
        this.configurationService = configurationService;
        this.searchService = searchService;
        this.objectMapper = objectMapper;
        this.itemsFileDir = projectRootProvider.getProjectRoot().resolve(ITEMS_FILE_DIR);
        if (!Files.exists(itemsFileDir)) {
            try {
                Files.createDirectories(itemsFileDir);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create vault items directory!", e);
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
        item.setTags(defaultTags);

        return item;
    }

    @RestrictResult
    @TranslateResult
    public Item load(String vaultItemId) {
        Optional<ItemEntity> vaultItemEntityOptional = itemEntityRepository.findById(vaultItemId);
        if (vaultItemEntityOptional.isPresent()) {
            TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();
            ItemEntity itemEntity = vaultItemEntityOptional.get();
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
            return item;
        }
        return null;
    }

    @GenerateIds
    public Item save(Item item) {
        ItemEntity itemEntity = itemEntityRepository.findById(item.getId()).orElse(new ItemEntity());

        itemEntity.setId(item.getId());
        itemEntity.setVersion(item.getVersion());
        itemEntity.setContentJson(toJson(item));

        List<String> imagesInVaultItem = item.getMediaContent().getImages();
        List<String> imagesToDelete = getFiles(getDirFromId(itemsFileDir, item.getId()), IMAGES_DIR);
        imagesToDelete.removeAll(imagesInVaultItem);
        imagesToDelete.forEach(imageToDelete -> {
            try {
                Files.deleteIfExists(getSubdirFilePath(itemsFileDir, item.getId(), IMAGES_DIR).resolve(imageToDelete));
                for (ImageSize imageSize : ImageSize.values()) {
                    Files.deleteIfExists(getSubdirFilePath(itemsFileDir, item.getId(), IMAGES_DIR)
                            .resolve(imageSize.name() + "-" + imageToDelete));
                }
            } catch (IOException e) {
                log.error("Could not delete obsolete image from filesystem!", e);
            }
        });

        List<String> modelsInVaultItem = item.getMediaContent().getModels();
        List<String> modelsToDelete = getFiles(getDirFromId(itemsFileDir, item.getId()), MODELS_DIR);
        modelsToDelete.removeAll(modelsInVaultItem);
        modelsToDelete.forEach(imageToDelete -> {
            try {
                Files.deleteIfExists(getSubdirFilePath(itemsFileDir, item.getId(), MODELS_DIR).resolve(imageToDelete));
            } catch (IOException e) {
                log.error("Could not delete obsolete model from filesystem!", e);
            }
        });

        ItemEntity savedItemEntity = itemEntityRepository.save(itemEntity);

        item.setVersion(savedItemEntity.getVersion());

        searchService.updateIndex(item);

        return item;
    }

    public void delete(String vaultItemId) {
        itemEntityRepository.deleteById(vaultItemId);
        deleteDirAndEmptyParents(getDirFromId(itemsFileDir, vaultItemId));
    }

    public FileSystemResource loadImage(String vaultItemId, String fileName, ImageSize targetSize) {
        return loadImage(itemsFileDir, vaultItemId, fileName, targetSize, IMAGES_DIR);
    }


    public FileSystemResource loadModel(String vaultItemId, String fileName) {
        return new FileSystemResource(itemsFileDir
                .resolve(vaultItemId.substring(0, 3))
                .resolve(vaultItemId.substring(3, 6))
                .resolve(vaultItemId)
                .resolve(MODELS_DIR)
                .resolve(fileName));
    }

    public List<String> getFilesForDownload(String vaultItemId) {
        List<String> result = new LinkedList<>();

        Item item = load(vaultItemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + vaultItemId + " found!");
        }

        result.addAll(item.getMediaContent().getImages().stream()
                .map(image -> itemsFileDir
                        .resolve(vaultItemId.substring(0, 3))
                        .resolve(vaultItemId.substring(3, 6))
                        .resolve(vaultItemId)
                        .resolve(IMAGES_DIR)
                        .resolve(image)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        result.addAll(item.getMediaContent().getModels().stream()
                .map(model -> itemsFileDir
                        .resolve(vaultItemId.substring(0, 3))
                        .resolve(vaultItemId.substring(3, 6))
                        .resolve(vaultItemId)
                        .resolve(MODELS_DIR)
                        .resolve(model)
                        .toAbsolutePath())
                .map(Path::toString)
                .toList());

        return result;
    }

    /**
     * Has to be synchronized because file-upload from the vault frontend is multi-threaded which leads to concurrent
     * modification exceptions if not synchronized!
     *
     * @param vaultItemId ID of the vault item.
     * @param file        The image file to save.
     */
    public void addImage(String vaultItemId, MultipartFile file) {
        Item item = load(vaultItemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + vaultItemId + " found!");
        }

        item.getMediaContent().getImages().add(
                saveFile(vaultItemId, file, IMAGES_DIR, null)
        );
        save(item);
    }

    /**
     * Has to be synchronized because file-upload from the vault frontend is multi-threaded which leads to concurrent
     * modification exceptions if not synchronized!
     *
     * @param vaultItemId ID of the vault item.
     * @param file        The model file to save.
     */
    public void addModel(String vaultItemId, MultipartFile file) {
        Item item = load(vaultItemId);

        if (item == null) {
            throw new IllegalArgumentException("No item with ID " + vaultItemId + " found!");
        }

        item.getMediaContent().getModels().add(
                saveFile(vaultItemId, file, MODELS_DIR, "glb")
        );
        save(item);
    }

    private String saveFile(String vaultItemId, MultipartFile file, String subDir, String requiredFileExtension) {
        Path targetDir = getSubdirFilePath(itemsFileDir, vaultItemId, subDir);

        int nextAssetNumber = getNextAssetNumber(targetDir);
        String fileExtension = getExtension(file.getOriginalFilename()).orElseThrow();

        if (StringUtils.hasText(requiredFileExtension) && !requiredFileExtension.equals(fileExtension)) {
            throw new VaultException("Unsupported file format. Files must be in '" + requiredFileExtension + "' format!");
        }

        String assetName = getAssetName(nextAssetNumber, fileExtension);

        Path targetPath = targetDir.resolve(assetName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new VaultException("Could not save model!", e);
        }

        return assetName;
    }

    private int getNextAssetNumber(Path assetDir) {
        var highestNumber = 0;
        if (!Files.exists(assetDir)) {
            try {
                Files.createDirectories(assetDir);
            } catch (IOException e) {
                throw new VaultException("Could not create asset directory!", e);
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
            throw new VaultException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    private String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

}
