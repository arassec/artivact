package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;

public interface LoadAppearanceConfigurationUseCase {

    /**
     * Loads the current appearance configuration of the application.
     *
     * @return The current appearance configuration.
     */
    AppearanceConfiguration loadTranslatedAppearanceConfiguration();

}
