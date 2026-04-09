package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AiConfiguration;

/**
 * Use case for saving the artificial intelligence configuration.
 */
public interface SaveAiConfigurationUseCase {

    /**
     * Saves an AI configuration.
     *
     * @param aiConfiguration The configuration to save.
     */
    void saveAiConfiguration(AiConfiguration aiConfiguration);

}
