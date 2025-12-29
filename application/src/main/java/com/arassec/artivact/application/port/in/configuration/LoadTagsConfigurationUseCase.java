package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.TagsConfiguration;

/**
 * Use case for load tags configuration operations.
 */
public interface LoadTagsConfigurationUseCase {

    /**
     * Loads the current tag configuration.
     *
     * @return The current tag configuration.
     */
    TagsConfiguration loadTagsConfiguration();

    /**
     * Loads the current tag configuration, restricted and translated.
     *
     * @return The current tag configuration.
     */
    TagsConfiguration loadTranslatedRestrictedTagsConfiguration();

}
