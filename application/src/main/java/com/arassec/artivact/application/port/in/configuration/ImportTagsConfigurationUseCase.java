package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.exchange.ImportContext;

/**
 * Use case for import tags configuration operations.
 */
public interface ImportTagsConfigurationUseCase {

    /**
     * Imports the tags configuration from the given String.
     *
     * @param tagsConfiguration The tags configuration as String.
     */
    void importTagsConfiguration(String tagsConfiguration);

    /**
     * Imports the tags configuration.
     *
     * @param importContext The import context.
     */
    void importTagsConfiguration(ImportContext importContext);

}
