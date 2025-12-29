package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;

/**
 * Use case for load appearance configuration operations.
 */
public interface LoadAppearanceConfigurationUseCase {

    /**
     * Loads the current appearance configuration of the application.
     *
     * @return The current appearance configuration.
     */
    AppearanceConfiguration loadTranslatedAppearanceConfiguration();

}
