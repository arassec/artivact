package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link TagsExporter}.
 */
@ExtendWith(MockitoExtension.class)
class TagsExporterTest {

    /**
     * The exporter under test.
     */
    @InjectMocks
    private TagsExporter exporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;


    /**
     * The export context.
     */
    private ExportContext exportContext;

    /**
     * The configuration to export.
     */
    private TagsConfiguration tagsConfiguration;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder().build());

        tagsConfiguration = TagsConfiguration.builder()
                .tags(List.of(
                        Tag.builder().value("one").build(),
                        Tag.builder().value("two").restrictions(Set.of("admin")).build(),
                        Tag.builder().value("three").build()
                ))
                .build();
    }

    /**
     * Tests exporting the tags configuration with restrictions
     */
    @Test
    @SneakyThrows
    void testExportTagsConfigurationWithRestrictions() {
        exportContext.getExportConfiguration().setApplyRestrictions(true);

        Path exportFile = exporter.exportTagsConfiguration(exportContext, tagsConfiguration);

        assertThat(exportFile).isEqualTo(Path.of("export-dir/artivact.tags-configuration.json"));

        ArgumentCaptor<TagsConfiguration> argCap = ArgumentCaptor.forClass(TagsConfiguration.class);
        verify(objectMapper).writeValue(eq(exportFile.toFile()), argCap.capture());

        TagsConfiguration exportedTagsConfiguration = argCap.getValue();
        assertThat(exportedTagsConfiguration.getTags()).hasSize(2);
        assertThat(exportedTagsConfiguration.getTags().get(0).getValue()).isEqualTo("one");
        assertThat(exportedTagsConfiguration.getTags().get(1).getValue()).isEqualTo("three");
    }

    /**
     * Tests exporting the tags configuration without restrictions
     */
    @Test
    @SneakyThrows
    void testExportTagsConfigurationWithoutRestrictions() {
        Path exportFile = exporter.exportTagsConfiguration(exportContext, tagsConfiguration);

        assertThat(exportFile).isEqualTo(Path.of("export-dir/artivact.tags-configuration.json"));

        ArgumentCaptor<TagsConfiguration> argCap = ArgumentCaptor.forClass(TagsConfiguration.class);
        verify(objectMapper).writeValue(eq(exportFile.toFile()), argCap.capture());

        TagsConfiguration exportedTagsConfiguration = argCap.getValue();
        assertThat(exportedTagsConfiguration.getTags()).hasSize(3);
    }

}
