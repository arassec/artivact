package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.service.BaseExportService;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.property.Property;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.arassec.artivact.domain.model.tag.Tag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationExchangeService extends BaseExportService
        implements ExportPropertiesConfigurationUseCase, ExportTagsConfigurationUseCase, ImportPropertiesConfigurationUseCase, ImportTagsConfigurationUseCase {

    /**
     * The application's object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    @Getter
    private final FileRepository fileRepository;

    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;
    private final SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    private final LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;
    private final SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;


    /**
     * Exports the current tags configuration and returns the result as String.
     *
     * @return The exported tags configuration.
     */
    @Override
    public String exportTagsConfiguration() {
        return fileRepository.read(exportTagsConfiguration(loadTagsConfigurationUseCase.loadTagsConfiguration()));
    }

    /**
     * Exports the current property configuration and returns the export result as String.
     *
     * @return The exported property configuration.
     */
    @Override
    public String exportPropertiesConfiguration() {
        return fileRepository.read(exportPropertiesConfiguration(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportPropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        return exportPropertiesConfiguration(ExportContext.builder()
                .exportDir(useProjectDirsUseCase.getProjectRoot().resolve(DirectoryDefinitions.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build(), propertiesConfiguration
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportTagsConfiguration(TagsConfiguration tagsConfiguration) {
        return exportTagsConfiguration(ExportContext.builder()
                .exportDir(useProjectDirsUseCase.getProjectRoot().resolve(DirectoryDefinitions.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build(), tagsConfiguration
        );
    }

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    @Override
    public Path exportTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration) {
        Path exportFile = exportContext.getExportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        cleanupTagsConfiguration(exportContext, tagsConfiguration);
        writeJsonFile(exportFile, tagsConfiguration);
        return exportFile;
    }

    /**
     * Cleans up tags configuration for export.
     *
     * @param exportContext     Export context.
     * @param tagsConfiguration The tag configuration to clean up.
     */
    private void cleanupTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration) {
        List<Tag> cleanedTags = new LinkedList<>();
        tagsConfiguration.getTags().stream()
                .filter(tag -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return tag.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(tag -> {
                    cleanupTranslations(tag);
                    cleanedTags.add(tag);
                });
        tagsConfiguration.setTags(cleanedTags);
    }

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    @Override
    public Path exportPropertiesConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration) {
        Path exportFile = exportContext.getExportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        PropertiesConfiguration cleanedPropertiesConfiguration = cleanPropertyConfiguration(exportContext, propertiesConfiguration);
        writeJsonFile(exportFile, cleanedPropertiesConfiguration);
        return exportFile;
    }

    @Override
    public void importPropertiesConfiguration(String propertiesConfiguration) {
        PropertiesConfiguration propConfig;
        try {
            propConfig = objectMapper.readValue(propertiesConfiguration, PropertiesConfiguration.class);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not deserialize properties configuration!", e);
        }
        savePropertiesConfigurationUseCase.savePropertiesConfiguration(propConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importPropertiesConfiguration(ImportContext importContext) {
        Path propertiesConfigurationJson = importContext.getImportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(propertiesConfigurationJson)) {
            PropertiesConfiguration propertiesConfiguration;
            try {
                propertiesConfiguration = objectMapper.readValue(fileRepository.read(propertiesConfigurationJson), PropertiesConfiguration.class);
            } catch (JsonProcessingException e) {
                throw new ArtivactException("Could not read properties configuration file", e);
            }
            savePropertiesConfigurationUseCase.savePropertiesConfiguration(propertiesConfiguration);
        }
    }

    @Override
    public void importTagsConfiguration(String tagsConfiguration) {
        try {
            TagsConfiguration tagsConfig = objectMapper.readValue(tagsConfiguration, TagsConfiguration.class);
            saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfig);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importTagsConfiguration(ImportContext importContext) {
        Path tagsConfigurationJson = importContext.getImportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(tagsConfigurationJson)) {
            TagsConfiguration tagsConfiguration;
            try {
                tagsConfiguration = objectMapper.readValue(fileRepository.read(tagsConfigurationJson), TagsConfiguration.class);
            } catch (JsonProcessingException e) {
                throw new ArtivactException("Could not read tags configuration file", e);
            }
            saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfiguration);
        }
    }

    /**
     * Cleans up property categories for export.
     *
     * @param exportContext           Export context.
     * @param propertiesConfiguration The property configuration to clean up.
     */
    private PropertiesConfiguration cleanPropertyConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration) {
        return PropertiesConfiguration.builder()
                .categories(cleanCategories(exportContext, propertiesConfiguration.getCategories()))
                .build();
    }

    /**
     * Cleans and filters categories.
     *
     * @param exportContext Export context.
     * @param categories    The categories to clean up.
     * @return Cleaned and filtered categories.
     */
    private List<PropertyCategory> cleanCategories(ExportContext exportContext, List<PropertyCategory> categories) {
        List<PropertyCategory> cleanedCategories = new LinkedList<>();
        categories.stream()
                .filter(propertyCategory -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(propertyCategory -> {
                    cleanupTranslations(propertyCategory);
                    propertyCategory.setProperties(cleanProperties(exportContext, propertyCategory.getProperties()));
                    cleanedCategories.add(propertyCategory);
                });
        return cleanedCategories;
    }

    /**
     * Cleans and filters properties.
     *
     * @param exportContext Export context.
     * @param properties    The properties to clean.
     * @return Cleaned and filtered properties.
     */
    private List<Property> cleanProperties(ExportContext exportContext, List<Property> properties) {
        List<Property> cleanedProperties = new LinkedList<>();
        properties.stream()
                .filter(property -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return property.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(property -> {
                    cleanupTranslations(property);
                    if (property.getValueRange() != null && !property.getValueRange().isEmpty()) {
                        property.setValueRange(cleanValueRange(exportContext, property));
                    }
                    cleanedProperties.add(property);
                });
        return cleanedProperties;
    }

    /**
     * Cleans and filters a property's value range.
     *
     * @param exportContext Export context.
     * @param property      The property to clean.
     * @return Cleaned and filtered value range.
     */
    private List<BaseTranslatableRestrictedObject> cleanValueRange(ExportContext exportContext, Property property) {
        List<BaseTranslatableRestrictedObject> cleanedValueRange = new LinkedList<>();
        property.getValueRange().stream()
                .filter(propertyValue -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return propertyValue.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(value -> {
                    cleanupTranslations(value);
                    cleanedValueRange.add(value);
                });
        return cleanedValueRange;
    }

}
