package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.PageService;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ArtivactStandardExporter}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactStandardExporterTest {

    /**
     * The tested exporter.
     */
    @InjectMocks
    private ArtivactStandardExporter exporter;

    /**
     * The service for page handling.
     */
    @Mock
    private PageService pageService;

    /**
     * The service for searching items.
     */
    @Mock
    private SearchService searchService;

    /**
     * The application's file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The application's project data provider.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests exporting the property configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        Path root = Path.of("exporterTest");

        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();

        Path exportedTagsConfiguration = exporter.exportPropertiesConfiguration(propertiesConfiguration);

        assertThat(exportedTagsConfiguration.toString()).hasToString("exporterTest/exports/artivact.properties-configuration.json");

        verify(objectMapper).writeValue(any(File.class), eq(propertiesConfiguration));
    }

    /**
     * Tests exporting the tags configuration.
     */
    @Test
    @SneakyThrows
    void testExportTagsConfiguration() {
        Path root = Path.of("exporterTest");

        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        TagsConfiguration tagsConfiguration = new TagsConfiguration();

        Path exportedTagsConfiguration = exporter.exportTagsConfiguration(tagsConfiguration);

        assertThat(exportedTagsConfiguration.toString()).hasToString("exporterTest/exports/artivact.tags-configuration.json");

        verify(objectMapper).writeValue(any(File.class), eq(tagsConfiguration));
    }

}
