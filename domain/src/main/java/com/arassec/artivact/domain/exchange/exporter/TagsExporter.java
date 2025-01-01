package com.arassec.artivact.domain.exchange.exporter;


import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;

/**
 * Exporter for {@link TagsConfiguration}s.
 */
@Component
@Getter
@RequiredArgsConstructor
public class TagsExporter extends BaseExporter {

    /**
     * The application's object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    public Path exportTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration) {
        Path exportFile = exportContext.getExportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        cleanupTagsConfiguration(exportContext, tagsConfiguration);
        writeJsonFile(exportFile, tagsConfiguration);
        return exportFile;
    }

    /**
     * Cleans up tags configuration for export.
     *
     * @param exportContext     Export context.
     * @param tagsConfiguration The tags configuration to clean up.
     */
    private void cleanupTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration) {
        List<Tag> cleanedTags = new LinkedList<>();
        tagsConfiguration.getTags().stream()
                .filter(tag -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return tag.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(tag -> {
                    cleanupTranslations(tag);
                    cleanedTags.add(tag);
                });
        tagsConfiguration.setTags(cleanedTags);
    }

}
