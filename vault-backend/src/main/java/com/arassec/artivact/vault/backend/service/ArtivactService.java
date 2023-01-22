package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.core.ArtivactVaultException;
import com.arassec.artivact.vault.backend.persistence.model.ArtivactEntity;
import com.arassec.artivact.vault.backend.persistence.repository.ArtivactEntityRepository;
import com.arassec.artivact.vault.backend.service.model.*;
import com.arassec.artivact.vault.backend.service.model.configuration.PropertiesConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ArtivactService extends BaseService {

    private final ConfigurationService configurationService;

    private final ArtivactEntityRepository artivactEntityRepository;

    private final String artivactsDir;

    private final TypeReference<HashMap<String, String>> propertiesTypeRef = new TypeReference<>() {
    };

    private final TypeReference<List<String>> tagsTypeRef = new TypeReference<>() {
    };

    public ArtivactService(ObjectMapper objectMapper, ArtivactEntityRepository artivactEntityRepository,
                           ConfigurationService configurationService, @Value("${artivact.vault.data.dir}") String artivactsDir) {
        super(objectMapper);
        this.configurationService = configurationService;
        this.artivactEntityRepository = artivactEntityRepository;
        this.artivactsDir = artivactsDir;
    }

    @Transactional
    public void scanArtivactsDir() {
        var now = LocalDateTime.now();
        scanArtivactsDirRecursively(Path.of(artivactsDir), now);
        artivactEntityRepository.deleteAllByScannedBefore(now.minusMinutes(1));
    }

    public Artivact loadArtivact(String artivactId, List<String> roles) {
        Optional<ArtivactEntity> entityOptional = artivactEntityRepository.findById(artivactId);
        return entityOptional.map(artivactEntity -> fromEntity(artivactEntity, roles)).orElse(null);
    }

    public List<Artivact> loadArtivacts(List<String> roles) {
        List<Artivact> result = new LinkedList<>();
        artivactEntityRepository.findAll().forEach(entity -> result.add(fromEntity(entity, roles)));
        return result;
    }

    public void saveArtivact(Artivact artivact) {
        var entity = artivactEntityRepository.findById(artivact.getId()).orElse(new ArtivactEntity());
        entity.setId(artivact.getId());
        entity.setVersion(artivact.getVersion());
        if (!StringUtils.hasText(artivact.getTitle().getId())) {
            artivact.getTitle().setId(UUID.randomUUID().toString());
        }
        entity.setTitle(toJson(artivact.getTitle()));
        if (!StringUtils.hasText(artivact.getDescription().getId())) {
            artivact.getDescription().setId(UUID.randomUUID().toString());
        }
        entity.setDescription(toJson(artivact.getDescription()));
        entity.setMediaContentJson(toJson(artivact.getMediaContent()));
        entity.setPropertiesJson(toJson(artivact.getProperties()));
        entity.setTagsJson(toJson(artivact.getTags().stream().map(TranslatableItem::getId).toList()));
        artivactEntityRepository.save(entity);
    }

    private Artivact fromEntity(ArtivactEntity entity, List<String> roles) {
        return Artivact.builder()
                .id(entity.getId())
                .version(entity.getVersion())
                .title(fromJson(entity.getTitle(), TranslatableItem.class))
                .description(fromJson(entity.getDescription(), TranslatableItem.class))
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

    private void scanArtivactsDirRecursively(Path root, LocalDateTime scanTime) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {

                if (path.getFileName().toString().equals("data.json")) {
                    log.debug("Found: {}", path.toAbsolutePath());

                    var artivactId = path.getParent().getFileName().toString();

                    log.debug("Checking artivact with ID: {}", artivactId);
                    Optional<ArtivactEntity> entityOptional = artivactEntityRepository.findById(artivactId);
                    ArtivactEntity entity;
                    if (entityOptional.isPresent()) {
                        log.debug("Found artivact, updating media content.");
                        entity = entityOptional.get();
                    } else {
                        log.debug("New artivact, saving data.");
                        entity = new ArtivactEntity();
                        entity.setId(artivactId);
                        entity.setVersion(0);
                        entity.setTitle("");
                    }

                    entity.setScanned(scanTime);

                    var newMediaContent = new MediaContent();
                    var scannedMediaContent = createMediaContent(path.getParent());
                    Set<String> existingImages = new HashSet<>();
                    if (StringUtils.hasText(entity.getMediaContentJson())) {
                        MediaContent existingMediaContent = fromJson(entity.getMediaContentJson(), MediaContent.class);
                        existingMediaContent.getImages().forEach(imageEntry ->
                                scannedMediaContent.getImages().forEach(scannedImageEntry -> {
                                    if (imageEntry.equals(scannedImageEntry)) {
                                        newMediaContent.getImages().add(scannedImageEntry);
                                        existingImages.add(scannedImageEntry);
                                    }
                                }));
                    }
                    scannedMediaContent.getImages().forEach(scannedImageEntry -> {
                        if (!existingImages.contains(scannedImageEntry)) {
                            newMediaContent.getImages().add(scannedImageEntry);
                        }
                    });
                    newMediaContent.setModels(scannedMediaContent.getModels());

                    entity.setMediaContentJson(toJson(newMediaContent));

                    artivactEntityRepository.save(entity);
                } else if (Files.isDirectory(path)) {
                    scanArtivactsDirRecursively(path, scanTime);
                }

            });
        } catch (IOException e) {
            throw new ArtivactVaultException("Could not scan artivacts directory!", e);
        }
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

}
