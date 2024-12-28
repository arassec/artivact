package com.arassec.artivact.domain.exchange.exp;

import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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
        cleanupPropertyConfiguration(exportContext, propertiesConfiguration);
        writeJsonFile(exportFile, propertiesConfiguration);
        return exportFile;
    }

    /**
     * Cleans up property categories for export.
     *
     * @param exportContext           Export context.
     * @param propertiesConfiguration The property configuration to clean up.
     */
    private void cleanupPropertyConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration) {
        propertiesConfiguration.getCategories().stream()
                .filter(propertyCategory -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(propertyCategory -> {
                    cleanupTranslations(propertyCategory);
                    propertyCategory.getProperties().stream()
                            .filter(property -> {
                                if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                                    return property.getRestrictions().isEmpty();
                                }
                                return true;
                            })
                            .forEach(property -> {
                                cleanupTranslations(property);
                                if (property.getValueRange() != null && !property.getValueRange().isEmpty()) {
                                    property.getValueRange().stream()
                                            .filter(propertyValue -> {
                                                if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                                                    return propertyValue.getRestrictions().isEmpty();
                                                }
                                                return true;
                                            })
                                            .forEach(this::cleanupTranslations);
                                }
                            });
                });
    }

}
