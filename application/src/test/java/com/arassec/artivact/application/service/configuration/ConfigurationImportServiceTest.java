package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.SavePropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveTagsConfigurationUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationImportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

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
    void testImportPropertiesConfigurationFromString() throws Exception {
        String json = "{\"key\":\"value\"}";
        PropertiesConfiguration config = new PropertiesConfiguration();
        when(objectMapper.readValue(json, PropertiesConfiguration.class)).thenReturn(config);

        service.importPropertiesConfiguration(json);

        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    @Test
    void testImportPropertiesConfigurationFromStringThrowsWrappedException() throws Exception {
        String json = "invalid";
        when(objectMapper.readValue(json, PropertiesConfiguration.class)).thenThrow(new JsonProcessingException("fail") {
        });

        assertThatThrownBy(() -> service.importPropertiesConfiguration(json))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not deserialize properties configuration!");
    }

    @Test
    void testImportPropertiesConfigurationFromImportContextWhenFileExists() throws Exception {
        Path file = importDir.resolve("artivact.properties-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("valid-json");

        PropertiesConfiguration config = new PropertiesConfiguration();
        when(objectMapper.readValue("valid-json", PropertiesConfiguration.class)).thenReturn(config);

        service.importPropertiesConfiguration(importContext);

        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    @Test
    void testImportPropertiesConfigurationFromImportContextHandlesJsonError() throws Exception {
        Path file = importDir.resolve("artivact.properties-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("bad-json");

        when(objectMapper.readValue("bad-json", PropertiesConfiguration.class)).thenThrow(new JsonProcessingException("fail") {
        });

        assertThatThrownBy(() -> service.importPropertiesConfiguration(importContext))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not read properties configuration file");
    }

    @Test
    void testImportPropertiesConfigurationFromImportContextWhenFileDoesNotExist() {
        Path file = importDir.resolve("artivact.properties-configuration.json");
        when(fileRepository.exists(file)).thenReturn(false);

        service.importPropertiesConfiguration(importContext);

        verifyNoInteractions(savePropertiesConfigurationUseCase);
    }

    @Test
    void testImportTagsConfigurationFromString() throws Exception {
        String json = "{\"tag\":\"value\"}";
        TagsConfiguration config = new TagsConfiguration();
        when(objectMapper.readValue(json, TagsConfiguration.class)).thenReturn(config);

        service.importTagsConfiguration(json);

        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    @Test
    void testImportTagsConfigurationFromStringThrowsWrappedException() throws Exception {
        String json = "invalid";
        when(objectMapper.readValue(json, TagsConfiguration.class)).thenThrow(new JsonProcessingException("fail") {
        });

        assertThatThrownBy(() -> service.importTagsConfiguration(json))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not deserialize tags configuration!");
    }

    @Test
    void testImportTagsConfigurationFromImportContextWhenFileExists() throws Exception {
        Path file = importDir.resolve("artivact.tags-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("valid-json");

        TagsConfiguration config = new TagsConfiguration();
        when(objectMapper.readValue("valid-json", TagsConfiguration.class)).thenReturn(config);

        service.importTagsConfiguration(importContext);

        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    @Test
    void testImportTagsConfigurationFromImportContextHandlesJsonError() throws Exception {
        Path file = importDir.resolve("artivact.tags-configuration.json");
        when(fileRepository.exists(file)).thenReturn(true);
        when(fileRepository.read(file)).thenReturn("bad-json");

        when(objectMapper.readValue("bad-json", TagsConfiguration.class)).thenThrow(new JsonProcessingException("fail") {
        });

        assertThatThrownBy(() -> service.importTagsConfiguration(importContext))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not read tags configuration file");
    }

    @Test
    void testImportTagsConfigurationFromImportContextWhenFileDoesNotExist() {
        service.importTagsConfiguration(importContext);
        verifyNoInteractions(saveTagsConfigurationUseCase);
    }

}
