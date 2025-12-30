package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.TagsConfiguration;

/**
 * Use case for save tags configuration operations.
 */
public interface SaveTagsConfigurationUseCase {

    /**
     * Saves a tag configuration.
     *
     * @param tagsConfiguration The configuration to save.
     */
    void saveTagsConfiguration(TagsConfiguration tagsConfiguration);

}
