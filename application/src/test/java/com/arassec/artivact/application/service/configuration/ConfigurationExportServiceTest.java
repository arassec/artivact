package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.property.Property;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.arassec.artivact.domain.model.tag.Tag;
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

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationExportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @InjectMocks
    private ConfigurationExportService service;

    private final Path exportsDir = Path.of("exports");

    @Test
    @SneakyThrows
    void testExportTagsConfigurationDirect() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);

        TagsConfiguration tagsConfig = new TagsConfiguration();
        tagsConfig.setTags(List.of(new Tag()));

        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(tagsConfig);
        when(fileRepository.read(any())).thenReturn("tags-json");

        String result = service.exportTagsConfiguration();

        assertThat(result).isEqualTo("tags-json");
        verify(loadTagsConfigurationUseCase).loadTagsConfiguration();
        verify(fileRepository).read(any(Path.class));

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(TagsConfiguration.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.tags-configuration.json");
    }

    @Test
    @SneakyThrows
    void testExportPropertiesConfigurationDirect() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);

        PropertiesConfiguration propsConfig = PropertiesConfiguration.builder().build();

        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(propsConfig);
        when(fileRepository.read(any())).thenReturn("props-json");

        String result = service.exportPropertiesConfiguration();

        assertThat(result).isEqualTo("props-json");
        verify(loadPropertiesConfigurationUseCase).loadPropertiesConfiguration();
        verify(fileRepository).read(any(Path.class));

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(PropertiesConfiguration.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.properties-configuration.json");
    }

    @Test
    void testExportTagsConfigurationWithContext() {
        TagsConfiguration tagsConfig = new TagsConfiguration();
        tagsConfig.setTags(List.of(new Tag()));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        Path result = service.exportTagsConfiguration(ctx, tagsConfig);

        assertThat(result.toString()).endsWith(TAGS_EXCHANGE_FILENAME_JSON);
        verify(fileRepository, never()).read(any());
    }

    @Test
    void testExportPropertiesConfigurationWithContext() {
        PropertyCategory category = new PropertyCategory();
        category.setProperties(List.of(new Property()));

        PropertiesConfiguration propsConfig = PropertiesConfiguration.builder()
                .categories(List.of(category))
                .build();

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        Path result = service.exportPropertiesConfiguration(ctx, propsConfig);

        assertThat(result.toString()).endsWith(PROPERTIES_EXCHANGE_FILENAME_JSON);
        verify(fileRepository, never()).read(any());
    }

}
