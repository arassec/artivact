package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;

/**
 * Use case for save properties configuration operations.
 */
public interface SavePropertiesConfigurationUseCase {

    /**
     * Saves a property configuration.
     *
     * @param propertiesConfiguration The configuration to save.
     */
    void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration);

}
