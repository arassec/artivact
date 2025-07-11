package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.exchange.ImportContext;

public interface ImportTagsConfigurationUseCase {

    void importTagsConfiguration(String tagsConfiguration);

    /**
     * Imports the tags configuration.
     *
     * @param importContext The import context.
     */
    void importTagsConfiguration(ImportContext importContext);

}
