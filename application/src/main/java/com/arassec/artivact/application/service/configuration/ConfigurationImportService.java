package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SavePropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveTagsConfigurationUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationImportService implements ImportPropertiesConfigurationUseCase, ImportTagsConfigurationUseCase {

    /**
     * The application's object mapper.
     */
    private final ObjectMapper objectMapper;

    private final FileRepository fileRepository;

    private final SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    private final SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;


    @Override
    public void importPropertiesConfiguration(String propertiesConfiguration) {
        PropertiesConfiguration propConfig;
        try {
            propConfig = objectMapper.readValue(propertiesConfiguration, PropertiesConfiguration.class);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not deserialize properties configuration!", e);
        }
        savePropertiesConfigurationUseCase.savePropertiesConfiguration(propConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importPropertiesConfiguration(ImportContext importContext) {
        Path propertiesConfigurationJson = importContext.getImportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(propertiesConfigurationJson)) {
            PropertiesConfiguration propertiesConfiguration;
            try {
                propertiesConfiguration = objectMapper.readValue(fileRepository.read(propertiesConfigurationJson), PropertiesConfiguration.class);
            } catch (JsonProcessingException e) {
                throw new ArtivactException("Could not read properties configuration file", e);
            }
            savePropertiesConfigurationUseCase.savePropertiesConfiguration(propertiesConfiguration);
        }
    }

    @Override
    public void importTagsConfiguration(String tagsConfiguration) {
        try {
            TagsConfiguration tagsConfig = objectMapper.readValue(tagsConfiguration, TagsConfiguration.class);
            saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfig);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not deserialize tags configuration!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importTagsConfiguration(ImportContext importContext) {
        Path tagsConfigurationJson = importContext.getImportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(tagsConfigurationJson)) {
            TagsConfiguration tagsConfiguration;
            try {
                tagsConfiguration = objectMapper.readValue(fileRepository.read(tagsConfigurationJson), TagsConfiguration.class);
            } catch (JsonProcessingException e) {
                throw new ArtivactException("Could not read tags configuration file", e);
            }
            saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfiguration);
        }
    }

}
