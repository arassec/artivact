package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AiConfiguration;

/**
 * Use case for loading the artificial intelligence configuration.
 */
public interface LoadAiConfigurationUseCase {

    /**
     * Loads the current AI configuration of the application.
     *
     * @return The current AI configuration.
     */
    AiConfiguration loadAiConfiguration();

}
