package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SavePropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveTagsConfigurationUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationImportService implements ImportPropertiesConfigurationUseCase, ImportTagsConfigurationUseCase {

    /**
     * The application's JSON mapper.
     */
    private final JsonMapper jsonMapper;

    private final FileRepository fileRepository;

    private final SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    private final SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;


    @Override
    public void importPropertiesConfiguration(String propertiesConfiguration) {
        PropertiesConfiguration propConfig;
        propConfig = jsonMapper.readValue(propertiesConfiguration, PropertiesConfiguration.class);
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
            propertiesConfiguration = jsonMapper.readValue(fileRepository.read(propertiesConfigurationJson), PropertiesConfiguration.class);
            savePropertiesConfigurationUseCase.savePropertiesConfiguration(propertiesConfiguration);
        }
    }

    @Override
    public void importTagsConfiguration(String tagsConfiguration) {
        TagsConfiguration tagsConfig = jsonMapper.readValue(tagsConfiguration, TagsConfiguration.class);
        saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importTagsConfiguration(ImportContext importContext) {
        Path tagsConfigurationJson = importContext.getImportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(tagsConfigurationJson)) {
            TagsConfiguration tagsConfiguration;
            tagsConfiguration = jsonMapper.readValue(fileRepository.read(tagsConfigurationJson), TagsConfiguration.class);
            saveTagsConfigurationUseCase.saveTagsConfiguration(tagsConfiguration);
        }
    }

}
