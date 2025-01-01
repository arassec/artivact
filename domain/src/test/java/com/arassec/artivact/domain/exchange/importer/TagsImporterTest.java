package com.arassec.artivact.domain.exchange.importer;

import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TagsImporter}.
 */
@ExtendWith(MockitoExtension.class)
class TagsImporterTest {

    /**
     * Importer under test.
     */
    @InjectMocks
    private TagsImporter tagsImporter;

    /**
     * The application's {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Service for configuration handling.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * Import context for testing.
     */
    private final ImportContext importContext = ImportContext.builder()
            .importDir(Path.of("import-dir"))
            .build();

    /**
     * Tests importing properties configuration.
     */
    @Test
    @SneakyThrows
    void testImportPropertiesConfiguration() {
        Path jsonFile = Path.of("import-dir/artivact.tags-configuration.json");
        when(fileRepository.exists(jsonFile)).thenReturn(true);
        when(fileRepository.read(jsonFile)).thenReturn("json-content");

        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        when(objectMapper.readValue("json-content", TagsConfiguration.class)).thenReturn(tagsConfiguration);

        tagsImporter.importTagsConfiguration(importContext);

        verify(configurationService).saveTagsConfiguration(tagsConfiguration);
    }

}
