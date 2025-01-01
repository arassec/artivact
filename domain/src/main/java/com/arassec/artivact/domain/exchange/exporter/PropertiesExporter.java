package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.property.Property;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;

/**
 * Exporter for {@link PropertiesConfiguration}s.
 */
@Component
@Getter
@RequiredArgsConstructor
public class PropertiesExporter extends BaseExporter {

    /**
     * The application's object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    public Path exportPropertiesConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration) {
        Path exportFile = exportContext.getExportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        PropertiesConfiguration cleanedPropertiesConfiguration = cleanPropertyConfiguration(exportContext, propertiesConfiguration);
        writeJsonFile(exportFile, cleanedPropertiesConfiguration);
        return exportFile;
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
