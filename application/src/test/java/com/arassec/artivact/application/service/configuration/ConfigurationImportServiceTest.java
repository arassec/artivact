package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.SavePropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveTagsConfigurationUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationImportServiceTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    @Mock
    private SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;

    @InjectMocks
    private ConfigurationImportService service;

    private ImportContext importContext;
    private Path importDir;

    @BeforeEach
    void setUp() {
        importDir = Path.of("/tmp/import");
        importContext = ImportContext.builder().importDir(importDir).build();
    }

    @Test
    void testImportPropertiesConfigurationFromString() {
        String json = "{\"key\":\"value\"}";
        PropertiesConfiguration config = new PropertiesConfiguration();
        when(jsonMapper.readValue(json, PropertiesConfiguration.class)).thenReturn(config);

        service.importPropertiesConfiguration(json);

        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    @Test
    void testImportPropertiesConfigurationFromImportContextWhenFileExists() {
        Path file = importDir.resolve("artivact.properties-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("valid-json");

        PropertiesConfiguration config = new PropertiesConfiguration();
        when(jsonMapper.readValue("valid-json", PropertiesConfiguration.class)).thenReturn(config);

        service.importPropertiesConfiguration(importContext);

        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    @Test
    void testImportPropertiesConfigurationFromImportContextWhenFileDoesNotExist() {
        Path file = importDir.resolve("artivact.properties-configuration.json");
        when(fileRepository.exists(file)).thenReturn(false);

        service.importPropertiesConfiguration(importContext);

        verifyNoInteractions(savePropertiesConfigurationUseCase);
    }

    @Test
    void testImportTagsConfigurationFromString() {
        String json = "{\"tag\":\"value\"}";
        TagsConfiguration config = new TagsConfiguration();
        when(jsonMapper.readValue(json, TagsConfiguration.class)).thenReturn(config);

        service.importTagsConfiguration(json);

        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    @Test
    void testImportTagsConfigurationFromImportContextWhenFileExists() {
        Path file = importDir.resolve("artivact.tags-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("valid-json");

        TagsConfiguration config = new TagsConfiguration();
        when(jsonMapper.readValue("valid-json", TagsConfiguration.class)).thenReturn(config);

        service.importTagsConfiguration(importContext);

        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    @Test
    void testImportTagsConfigurationFromImportContextWhenFileDoesNotExist() {
        service.importTagsConfiguration(importContext);
        verifyNoInteractions(saveTagsConfigurationUseCase);
    }

}
