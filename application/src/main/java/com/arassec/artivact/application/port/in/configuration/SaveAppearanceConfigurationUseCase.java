package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;

public interface SaveAppearanceConfigurationUseCase {

    /**
     * Saves an appearance configuration.
     *
     * @param appearanceConfiguration The configuration to save.
     */
    void saveAppearanceConfiguration(AppearanceConfiguration appearanceConfiguration);

}
