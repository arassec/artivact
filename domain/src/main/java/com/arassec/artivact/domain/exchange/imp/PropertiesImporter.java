package com.arassec.artivact.domain.exchange.imp;

import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;

/**
 * Importer for {@link PropertiesConfiguration}s.
 */
@Component
@RequiredArgsConstructor
public class PropertiesImporter {

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
     * Imports the properties configuration.
     *
     * @param importContext The import context.
     * @throws JsonProcessingException In case of parsing errors.
     */
    public void importPropertiesConfiguration(ImportContext importContext) throws JsonProcessingException {
        Path propertiesConfigurationJson = importContext.getImportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(propertiesConfigurationJson)) {
            PropertiesConfiguration propertiesConfiguration = objectMapper.readValue(fileRepository.read(propertiesConfigurationJson), PropertiesConfiguration.class);
            configurationService.savePropertiesConfiguration(propertiesConfiguration);
        }
    }

}
