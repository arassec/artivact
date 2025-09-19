package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.configuration.ExportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ExportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ExportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.menu.Menu;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionExportServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

    @Mock
    private ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

    @Mock
    private ExportMenuUseCase exportMenuUseCase;

    @InjectMocks
    private CollectionExportService service;

    private Path exportsDir;

    @BeforeEach
    void setup() {
        exportsDir = Path.of("exports");
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
    }

    @Test
    @SneakyThrows
    void testExportCollectionWithoutCoverPicture() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId("col1");

        Menu menu = new Menu();
        menu.setId("menu1");

        PropertiesConfiguration propsConfig = new PropertiesConfiguration();
        TagsConfiguration tagsConfig = new TagsConfiguration();

        Path result = service.exportCollection(collectionExport, menu, propsConfig, tagsConfig);

        assertThat(result.toString()).endsWith("col1.artivact.collection.zip");

        verify(exportPropertiesConfigurationUseCase).exportPropertiesConfiguration(any(), eq(propsConfig));
        verify(exportTagsConfigurationUseCase).exportTagsConfiguration(any(), eq(tagsConfig));
        verify(exportMenuUseCase).exportMenu(any(), eq(menu));

        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());
        verify(fileRepository, never()).copy(any(), any());

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(ExchangeMainData.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.content.json");
    }

    @Test
    @SneakyThrows
    void testExportCollectionWithCoverPictureFileExists() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId("col2");
        collectionExport.setCoverPictureExtension("jpg");

        Menu menu = new Menu();
        menu.setId("menu2");

        PropertiesConfiguration propsConfig = new PropertiesConfiguration();
        TagsConfiguration tagsConfig = new TagsConfiguration();

        Path coverFile = exportsDir.resolve("col2.jpg");
        lenient().doReturn(true).when(fileRepository).exists(coverFile);

        Path result = service.exportCollection(collectionExport, menu, propsConfig, tagsConfig);

        assertThat(result.toString()).endsWith("col2.artivact.collection.zip");

        verify(fileRepository).exists(coverFile);
        verify(fileRepository).copy(eq(coverFile), argThat(p -> p.toString().endsWith("cover-picture.jpg")), eq(StandardCopyOption.REPLACE_EXISTING));
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(ExchangeMainData.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.content.json");
    }

    @Test
    @SneakyThrows
    void testExportCollectionWithCoverPictureFileMissing() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId("col3");
        collectionExport.setCoverPictureExtension("png");

        Menu menu = new Menu();
        menu.setId("menu3");

        PropertiesConfiguration propsConfig = new PropertiesConfiguration();
        TagsConfiguration tagsConfig = new TagsConfiguration();

        Path coverFile = exportsDir.resolve("col3.png");
        lenient().doReturn(false).when(fileRepository).exists(coverFile);

        Path result = service.exportCollection(collectionExport, menu, propsConfig, tagsConfig);

        assertThat(result.toString()).endsWith("col3.artivact.collection.zip");

        verify(fileRepository).exists(coverFile);
        verify(fileRepository, never()).copy(any(), any());
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(ExchangeMainData.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.content.json");
    }

}
