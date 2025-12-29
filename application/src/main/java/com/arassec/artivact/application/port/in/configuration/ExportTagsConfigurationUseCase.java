package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;

import java.nio.file.Path;

/**
 * Use case for export tags configuration operations.
 */
public interface ExportTagsConfigurationUseCase {

    /**
     * Exports the current tags configuration and returns the result as String.
     *
     * @return The exported tags configuration.
     */
    String exportTagsConfiguration();

    /**
     * Exports the current tag configuration.
     *
     * @param tagsConfiguration The current tags configuration.
     * @return Path to the export file.
     */
    Path exportTagsConfiguration(TagsConfiguration tagsConfiguration);

    /**
     * Exports the current tags configuration into an export file.
     *
     * @param exportContext     Export parameters.
     * @param tagsConfiguration The tag configuration.
     * @return Path to the newly created export file containing the tag configuration.
     */
    Path exportTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration);

}
