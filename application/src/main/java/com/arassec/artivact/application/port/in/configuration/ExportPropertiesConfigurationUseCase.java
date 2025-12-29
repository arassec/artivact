package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;

import java.nio.file.Path;

/**
 * Use case for export properties configuration operations.
 */
public interface ExportPropertiesConfigurationUseCase {

    /**
     * Exports the current property configuration and returns the export result as String.
     *
     * @return The exported property configuration.
     */
    String exportPropertiesConfiguration();

    /**
     * Exports the current property configuration.
     *
     * @param propertiesConfiguration The current property configuration.
     * @return Path to the export.
     */
    Path exportPropertiesConfiguration(PropertiesConfiguration propertiesConfiguration);

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    Path exportPropertiesConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration);

}
