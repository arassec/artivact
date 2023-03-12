package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.common.exception.ArtivactException;
import com.arassec.artivact.common.model.Artivact;
import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.persistence.model.ArtivactEntity;
import com.arassec.artivact.vault.backend.persistence.repository.ArtivactEntityRepository;
import com.arassec.artivact.vault.backend.service.model.*;
import com.arassec.artivact.vault.backend.service.model.configuration.PropertiesConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class ArtivactService extends BaseService {

    private final ConfigurationService configurationService;

    private final ArtivactEntityRepository artivactEntityRepository;

    private final FileUtil fileUtil;

    @Getter
    private final Path projectRoot;

    private final TypeReference<HashMap<String, String>> propertiesTypeRef = new TypeReference<>() {
    };

    private final TypeReference<List<String>> tagsTypeRef = new TypeReference<>() {
    };

    public ArtivactService(ObjectMapper objectMapper, ArtivactEntityRepository artivactEntityRepository,
                           ConfigurationService configurationService, @Value("${artivact.vault.project.root}") String projectRootString,
                           FileUtil fileUtil) {
        super(objectMapper);
        this.configurationService = configurationService;
        this.artivactEntityRepository = artivactEntityRepository;
        this.projectRoot = Path.of(projectRootString);
        this.fileUtil = fileUtil;
    }

    @Transactional
    public void scanDataDir() {
        var now = LocalDateTime.now();
        scanDataDirRecursively(projectRoot.resolve(Artivact.DATA_DIR), now);
        artivactEntityRepository.deleteAllByScannedBefore(now.minusMinutes(1));
    }

    @Transactional
    public VaultArtivact createArtivact() {
        VaultArtivact vaultArtivact = VaultArtivact.builder()
                .id(UUID.randomUUID().toString())
                .version(0)
                .projectRoot(projectRoot)
                .title(new TranslatableItem())
                .description(new TranslatableItem())
                .mediaContent(new MediaContent())
                .build();

        try {
            if (!Files.exists(vaultArtivact.getImagesDir(true))) {
                Files.createDirectories(vaultArtivact.getImagesDir(true));
            }
            if (!Files.exists(vaultArtivact.getScaledImagesDir(true))) {
                Files.createDirectories(vaultArtivact.getScaledImagesDir(true));
            }
            if (!Files.exists(vaultArtivact.getModelsDir(true))) {
                Files.createDirectories(vaultArtivact.getModelsDir(true));
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not create artivact directories!", e);
        }

        saveArtivact(vaultArtivact);

        return vaultArtivact;
    }

    @Transactional
    public VaultArtivact loadArtivact(String artivactId, List<String> roles) {
        Optional<ArtivactEntity> entityOptional = artivactEntityRepository.findById(artivactId);
        Optional<VaultArtivact> vaultArtivactOptional = entityOptional.map(artivactEntity -> fromEntity(artivactEntity, roles));
        if (vaultArtivactOptional.isPresent()) {
            VaultArtivact vaultArtivact = vaultArtivactOptional.get();
            if (isAllowed(vaultArtivact.getRestrictions(), roles)) {
                return vaultArtivact;
            }
        }
        return null;
    }

    @Transactional
    public List<VaultArtivact> loadArtivacts(List<String> roles) {
        return StreamSupport.stream(artivactEntityRepository.findAll().spliterator(), false)
                .map(entity -> fromEntity(entity, roles))
                .filter(artivact -> isAllowed(artivact.getRestrictions(), roles))
                .toList();
    }

    @Transactional
    public void saveArtivact(VaultArtivact vaultArtivact) {
        var entity = artivactEntityRepository.findById(vaultArtivact.getId()).orElse(new ArtivactEntity());
        entity.setId(vaultArtivact.getId());
        entity.setVersion(vaultArtivact.getVersion());
        entity.setRestrictions(getRestrictions(vaultArtivact.getRestrictions()));
        if (!vaultArtivact.getRestrictions().isEmpty()) {
            entity.setRestrictions(String.join(",", vaultArtivact.getRestrictions()));
        }
        if (!StringUtils.hasText(vaultArtivact.getTitle().getId())) {
            vaultArtivact.getTitle().setId(UUID.randomUUID().toString());
        }
        entity.setTitleJson(toJson(vaultArtivact.getTitle()));
        if (!StringUtils.hasText(vaultArtivact.getDescription().getId())) {
            vaultArtivact.getDescription().setId(UUID.randomUUID().toString());
        }
        entity.setDescriptionJson(toJson(vaultArtivact.getDescription()));
        entity.setPropertiesJson(toJson(vaultArtivact.getProperties()));
        entity.setTagsJson(toJson(vaultArtivact.getTags().stream().map(TranslatableItem::getId).toList()));

        entity.setMediaContentJson(toJson(vaultArtivact.getMediaContent()));
        updateMediaContent(entity, vaultArtivact.getMainDir(true), true);

        artivactEntityRepository.save(entity);
    }

    @Transactional
    public void deleteArtivact(String artivactId, List<String> roles) {
        VaultArtivact vaultArtivact = loadArtivact(artivactId, roles);
        artivactEntityRepository.deleteById(artivactId);
        fileUtil.deleteArtivactDir(vaultArtivact);
    }

    private VaultArtivact fromEntity(ArtivactEntity entity, List<String> roles) {
        return VaultArtivact.builder()
                .id(entity.getId())
                .version(entity.getVersion())
                .projectRoot(projectRoot)
                .restrictions(getRestrictions(entity.getRestrictions()))
                .title(fromJson(entity.getTitleJson(), TranslatableItem.class))
                .description(fromJson(entity.getDescriptionJson(), TranslatableItem.class))
                .mediaContent(fromJson(entity.getMediaContentJson(), MediaContent.class))
                .properties(readPropertiesJson(entity.getPropertiesJson(), roles))
                .tags(readTagsJson(entity.getTagsJson(), roles))
                .build();
    }

    private List<Tag> readTagsJson(String tagsJson, List<String> roles) {
        if (!StringUtils.hasText(tagsJson)) {
            return new LinkedList<>();
        }
        try {
            List<String> configuredTagIds = getObjectMapper().readValue(tagsJson, tagsTypeRef);

            return configurationService.loadTagsConfiguration(roles).getTags().stream()
                    .filter(tag -> configuredTagIds.contains(tag.getId()))
                    .filter(tag -> isAllowed(Optional.of(tag), roles))
                    .toList();

        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert tags JSON-String to object!", e);
        }
    }

    private Map<String, String> readPropertiesJson(String json, List<String> roles) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            PropertiesConfiguration propertiesConfiguration = configurationService.loadPropertiesConfiguration(roles);
            HashMap<String, String> unfilteredProperties = getObjectMapper().readValue(json, propertiesTypeRef);

            return unfilteredProperties.entrySet().stream()
                    .filter(entry -> isAllowedProperty(entry.getKey(), propertiesConfiguration, roles))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (JsonProcessingException e) {
            throw new ArtivactVaultException("Could not convert JSON-String to object!", e);
        }
    }

    private boolean isAllowedProperty(String propertyId, PropertiesConfiguration propertiesConfiguration, List<String> roles) {
        Optional<PropertyCategory> matchingCategoryOptional = propertiesConfiguration.getCategories().stream()
                .filter(category -> category.getProperties().stream()
                        .anyMatch(property -> property.getId().equals(propertyId)))
                .findFirst();

        if (matchingCategoryOptional.isEmpty() || !isAllowed(matchingCategoryOptional, roles)) {
            return false;
        }

        PropertyCategory matchingCategory = matchingCategoryOptional.get();
        Optional<Property> matchingPropertyOptional = matchingCategory.getProperties().stream()
                .filter(property -> property.getId().equals(propertyId))
                .findFirst();

        return isAllowed(matchingPropertyOptional, roles);
    }

    private void scanDataDirRecursively(Path root, LocalDateTime scanTime) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {

                if (path.getFileName().toString().equals("data.json")) {
                    log.debug("Found: {}", path.toAbsolutePath());

                    var artivactId = path.getParent().getFileName().toString();

                    log.debug("Checking artivact with ID: {}", artivactId);
                    Optional<ArtivactEntity> entityOptional = artivactEntityRepository.findById(artivactId);
                    ArtivactEntity entity;
                    if (entityOptional.isPresent()) {
                        log.info("Updating media content of existing artivact: {}", artivactId);
                        entity = entityOptional.get();
                    } else {
                        log.info("Importing new artivact: {}", artivactId);
                        entity = new ArtivactEntity();
                        entity.setId(artivactId);
                        entity.setVersion(0);
                        entity.setTitleJson("");
                    }

                    entity.setScanned(scanTime);

                    updateMediaContent(entity, path.getParent(), false);

                    artivactEntityRepository.save(entity);
                } else if (Files.isDirectory(path)) {
                    scanDataDirRecursively(path, scanTime);
                }

            });
        } catch (IOException e) {
            throw new ArtivactVaultException("Could not scan artivacts directory!", e);
        }
    }

    private void updateMediaContent(ArtivactEntity entity, Path artivactDir, boolean cleanup) {
        var newMediaContent = new MediaContent();
        var scannedMediaContent = createMediaContent(artivactDir);
        Set<String> existingImages = new HashSet<>();
        if (StringUtils.hasText(entity.getMediaContentJson())) {
            MediaContent existingMediaContent = fromJson(entity.getMediaContentJson(), MediaContent.class);

            existingMediaContent.getImages().forEach(existingImageEntry ->
                    scannedMediaContent.getImages().forEach(scannedImageEntry -> {
                        if (existingImageEntry.equals(scannedImageEntry)) {
                            newMediaContent.getImages().add(scannedImageEntry);
                            existingImages.add(scannedImageEntry);
                        }
                    }));

            if (cleanup) {
                scannedMediaContent.getImages().stream()
                        .filter(image -> !existingMediaContent.getImages().contains(image))
                        .forEach(obsoleteImage -> {
                            try {
                                Files.delete(artivactDir.resolve(Artivact.IMAGES_DIR).resolve(obsoleteImage));
                                Files.list(artivactDir.resolve(VaultArtivact.SCALED_IMAGES_DIR)).forEach(
                                        scaledImageFile -> {
                                            if (scaledImageFile.getFileName().toString().endsWith(obsoleteImage)) {
                                                try {
                                                    Files.delete(scaledImageFile);
                                                } catch (IOException e) {
                                                    throw new ArtivactException("Could not delete obsolete scaled image!", e);
                                                }
                                            }
                                        }
                                );
                            } catch (IOException e) {
                                throw new ArtivactException("Could not delete obsolete image!", e);
                            }
                        });

                scannedMediaContent.getModels().stream()
                        .filter(model -> !existingMediaContent.getModels().contains(model))
                        .forEach(obsoleteModel -> {
                            try {
                                Files.delete(artivactDir.resolve(Artivact.MODELS_DIR).resolve(obsoleteModel));
                            } catch (IOException e) {
                                throw new ArtivactException("Could not delete obsolete model!", e);
                            }
                        });
            }

            newMediaContent.setModels(existingMediaContent.getModels());
        }

        if (!cleanup) {
            scannedMediaContent.getImages().forEach(scannedImageEntry -> {
                if (!existingImages.contains(scannedImageEntry)) {
                    newMediaContent.getImages().add(scannedImageEntry);
                }
            });
            newMediaContent.setModels(scannedMediaContent.getModels());
        }

        entity.setMediaContentJson(toJson(newMediaContent));
    }

    private MediaContent createMediaContent(Path artivactDir) {
        var result = new MediaContent();

        var imagesDir = Path.of(artivactDir.toString(), "images");
        if (Files.exists(imagesDir)) {
            try (Stream<Path> files = Files.list(imagesDir)) {
                result.setImages(files
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .toList()
                );
            } catch (IOException e) {
                throw new ArtivactVaultException("Could not scan images of artivact: " + artivactDir, e);
            }
        }

        var modelsDir = Path.of(artivactDir.toString(), "models");
        if (Files.exists(modelsDir)) {
            try (Stream<Path> files = Files.list(modelsDir)) {
                result.setModels(files
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .toList()
                );
            } catch (IOException e) {
                throw new ArtivactVaultException("Could not scan images of artivact: " + artivactDir, e);
            }
        }

        return result;
    }

    private List<String> getRestrictions(String input) {
        if (StringUtils.hasText(input)) {
            return List.of(input.split(","));
        }
        return new LinkedList<>();
    }

    private String getRestrictions(List<String> input) {
        if (input != null && !input.isEmpty()) {
            return String.join(",", input);
        }
        return null;
    }

}
