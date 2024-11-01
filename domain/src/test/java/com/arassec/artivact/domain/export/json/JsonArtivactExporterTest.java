package com.arassec.artivact.domain.export.json;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.property.Property;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.export.json.model.ContentExportFile;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.PageService;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JsonArtivactExporter}.
 */
@ExtendWith(MockitoExtension.class)
class JsonArtivactExporterTest {

    /**
     * The exporter under test.
     */
    @InjectMocks
    private JsonArtivactExporter jsonArtivactExporter;

    /**
     * The service for configuration handling.
     */
    @Mock
    private ConfigurationService configurationService;

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
     * A mocked progress monitor.
     */
    @Mock
    private ProgressMonitor progressMonitor;

    /**
     * Tests that the JSON exporter supports the export type JSON...
     */
    @Test
    void testSupports() {
        assertEquals(ExportType.JSON, jsonArtivactExporter.supports());
    }

    /**
     * Tests the standard export with default parameters as configured in the frontend.
     */
    @Test
    @SneakyThrows
    void testStandardExport() {
        mockPropertiesConfiguration();
        mockTagsConfiguration();

        var params = ExportParams.builder()
                .exportDir(Path.of("exports"))
                .contentExportDir(Path.of("exports/test"))
                .exportType(ExportType.JSON)
                .applyRestrictions(false)
                .optimizeSize(true)
                .zipResults(true)
                .build();

        Menu menu = new Menu();
        menu.setId("menu-id");
        menu.setExportTitle(TranslatableString.builder().value("export-title").build());
        menu.setExportDescription(TranslatableString.builder().value("export-description").build());

        jsonArtivactExporter.export(params, menu, progressMonitor);

        ArgumentCaptor<File> omFileArgCap = ArgumentCaptor.forClass(File.class);
        ArgumentCaptor<Object> omObjectArgCap = ArgumentCaptor.forClass(Object.class);
        verify(objectMapper, times(2)).writeValue(omFileArgCap.capture(), omObjectArgCap.capture());

        // General export of e.g. property and tag definitions:
        var file = omFileArgCap.getAllValues().getFirst();
        assertTrue(file.toString().replace("\\", "/")
                .endsWith("/exports/test/artivact.content.json"));
        var contentExportFile = (ContentExportFile) omObjectArgCap.getAllValues().getFirst();
        assertEquals("export-title", contentExportFile.getTitle().getValue());
        assertEquals("export-description", contentExportFile.getDescription().getValue());
        assertEquals("test-property", contentExportFile.getPropertyCategories().getFirst().getProperties().getFirst().getValue());
        assertEquals("test-tag", contentExportFile.getTags().getFirst().getValue());

        // Menu export:
        file = omFileArgCap.getAllValues().get(1);
        System.out.println(file.toString());
        assertTrue(file.toString().replace("\\", "/")
                .endsWith("/exports/test/menu-id.artivact.menu.json"));
        var exportedMenu = (Menu) omObjectArgCap.getAllValues().get(1);
        assertEquals("export-title", exportedMenu.getExportTitle().getValue());

        // TODO: Test Item export.
    }


    private void mockPropertiesConfiguration() {
        var propertiesConfiguration = PropertiesConfiguration.builder()
                .categories(List.of(
                        PropertyCategory.builder()
                                .properties(List.of(
                                        Property.builder().value("test-property").build()
                                )).build()
                )).build();

        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);
    }

    private void mockTagsConfiguration() {
        var tagsConfiguration = TagsConfiguration.builder()
                .tags(List.of(
                        Tag.builder().value("test-tag").url("test-url").defaultTag(true).build()
                ))
                .build();

        when(configurationService.loadTagsConfiguration()).thenReturn(tagsConfiguration);
    }

}
