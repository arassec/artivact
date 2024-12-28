package com.arassec.artivact.domain.exchange.imp;

import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;

/**
 * Importer for {@link TagsConfiguration}s.
 */
@Component
@RequiredArgsConstructor
public class TagsImporter {

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Service for configuration handling.
     */
    private final ConfigurationService configurationService;

    /**
     * Imports the tags configuration.
     *
     * @param importContext The import context.
     * @throws JsonProcessingException In case of parsing errors.
     */
    public void importTagsConfiguration(ImportContext importContext) throws JsonProcessingException {
        Path tagsConfigurationJson = importContext.getImportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(tagsConfigurationJson)) {
            TagsConfiguration tagsConfiguration = objectMapper.readValue(fileRepository.read(tagsConfigurationJson), TagsConfiguration.class);
            configurationService.saveTagsConfiguration(tagsConfiguration);
        }
    }

}
