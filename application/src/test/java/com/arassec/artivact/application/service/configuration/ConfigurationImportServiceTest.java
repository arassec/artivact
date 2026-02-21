package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.SavePropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SaveTagsConfigurationUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationImportServiceTest {

    @InjectMocks
    private ConfigurationImportService service;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    @Mock
    private SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;

    // --- importPropertiesConfiguration(String) ---

    @Test
    void testImportPropertiesConfigurationFromStringDeserializesAndSaves() {
        // Given
        String json = "{\"key\":\"value\"}";
        PropertiesConfiguration config = new PropertiesConfiguration();

        when(jsonMapper.readValue(json, PropertiesConfiguration.class)).thenReturn(config);

        // When
        service.importPropertiesConfiguration(json);

        // Then
        verify(jsonMapper).readValue(json, PropertiesConfiguration.class);
        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    // --- importPropertiesConfiguration(ImportContext) ---

    @Test
    void testImportPropertiesConfigurationFromContextWhenFileExists() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Path propertiesJson = Path.of("import")
                .resolve(DirectoryDefinitions.CONFIGS_DIR)
                .resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);

        when(fileRepository.exists(propertiesJson)).thenReturn(true);

        String jsonContent = "{\"properties\":true}";
        when(fileRepository.read(propertiesJson)).thenReturn(jsonContent);

        PropertiesConfiguration config = new PropertiesConfiguration();
        when(jsonMapper.readValue(jsonContent, PropertiesConfiguration.class)).thenReturn(config);

        // When
        service.importPropertiesConfiguration(importContext);

        // Then
        verify(fileRepository).read(propertiesJson);
        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(config);
    }

    @Test
    void testImportPropertiesConfigurationFromContextSkipsWhenFileDoesNotExist() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Path propertiesJson = Path.of("import")
                .resolve(DirectoryDefinitions.CONFIGS_DIR)
                .resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);

        when(fileRepository.exists(propertiesJson)).thenReturn(false);

        // When
        service.importPropertiesConfiguration(importContext);

        // Then
        verify(fileRepository, never()).read(any());
        verify(savePropertiesConfigurationUseCase, never()).savePropertiesConfiguration(any());
    }

    // --- importTagsConfiguration(String) ---

    @Test
    void testImportTagsConfigurationFromStringDeserializesAndSaves() {
        // Given
        String json = "{\"tags\":[]}";
        TagsConfiguration config = new TagsConfiguration();

        when(jsonMapper.readValue(json, TagsConfiguration.class)).thenReturn(config);

        // When
        service.importTagsConfiguration(json);

        // Then
        verify(jsonMapper).readValue(json, TagsConfiguration.class);
        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    // --- importTagsConfiguration(ImportContext) ---

    @Test
    void testImportTagsConfigurationFromContextWhenFileExists() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Path tagsJson = Path.of("import")
                .resolve(DirectoryDefinitions.CONFIGS_DIR)
                .resolve(TAGS_EXCHANGE_FILENAME_JSON);

        when(fileRepository.exists(tagsJson)).thenReturn(true);

        String jsonContent = "{\"tags\":[\"tag1\"]}";
        when(fileRepository.read(tagsJson)).thenReturn(jsonContent);

        TagsConfiguration config = new TagsConfiguration();
        when(jsonMapper.readValue(jsonContent, TagsConfiguration.class)).thenReturn(config);

        // When
        service.importTagsConfiguration(importContext);

        // Then
        verify(fileRepository).read(tagsJson);
        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(config);
    }

    @Test
    void testImportTagsConfigurationFromContextSkipsWhenFileDoesNotExist() {
        // Given
        ImportContext importContext = ImportContext.builder()
                .importDir(Path.of("import"))
                .build();

        Path tagsJson = Path.of("import")
                .resolve(DirectoryDefinitions.CONFIGS_DIR)
                .resolve(TAGS_EXCHANGE_FILENAME_JSON);

        when(fileRepository.exists(tagsJson)).thenReturn(false);

        // When
        service.importTagsConfiguration(importContext);

        // Then
        verify(fileRepository, never()).read(any());
        verify(saveTagsConfigurationUseCase, never()).saveTagsConfiguration(any());
    }
}
