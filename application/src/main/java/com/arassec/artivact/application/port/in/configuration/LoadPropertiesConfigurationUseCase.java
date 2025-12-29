package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.property.PropertyCategory;

import java.util.List;

/**
 * Use case for load properties configuration operations.
 */
public interface LoadPropertiesConfigurationUseCase {

    /**
     * Loads the current property configuration.
     *
     * @return The properties currently configured.
     */
    PropertiesConfiguration loadPropertiesConfiguration();

    /**
     * Loads the translated properties within their categories.
     *
     * @return The current properties.
     */
    List<PropertyCategory> loadTranslatedRestrictedProperties();

}
