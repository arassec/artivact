package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.configuration.ExportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ExportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemExportServiceTest {

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

    @Mock
    private ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @InjectMocks
    private ItemExportService service;

    private final Path exportsDir = Path.of("exports");

    private Item createItem(String id, List<String> images, List<String> models) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));
        item.setMediaContent(new MediaContent());
        item.getMediaContent().setImages(images);
        item.getMediaContent().setModels(models);
        return item;
    }

    @Test
    @SneakyThrows
    void testExportItemById() {
        Item item = createItem("item1", List.of(), List.of());
        PropertiesConfiguration propsConfig = PropertiesConfiguration.builder().build();
        TagsConfiguration tagsConfig = new TagsConfiguration();

        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        when(loadItemUseCase.loadTranslated("item1")).thenReturn(item);
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(propsConfig);
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(tagsConfig);

        Path result = service.exportItem("item1");

        assertThat(result.toString()).endsWith("item1.artivact.collection.zip");
        verify(loadItemUseCase).loadTranslated("item1");
        verify(exportPropertiesConfigurationUseCase).exportPropertiesConfiguration(any(), eq(propsConfig));
        verify(exportTagsConfigurationUseCase).exportTagsConfiguration(any(), eq(tagsConfig));
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(objectMapper).writeValue(argCap.capture(), any(Item.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.item.json");
    }

    @Test
    void testExportItemSkipsIfAlreadyExported() {
        Item item = createItem("item2", List.of(), List.of());
        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        Path itemDir = exportsDir.resolve("item2");
        when(fileRepository.exists(itemDir)).thenReturn(true);

        service.exportItem(ctx, item);

        verify(fileRepository, never()).createDirIfRequired(any());
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
    }

    @Test
    void testExportItemWritesJsonIfNotExists() {
        Item item = createItem("item3", List.of("img1.png"), List.of("model1.glb"));
        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        Path itemDir = exportsDir.resolve("item3");
        when(fileRepository.exists(itemDir)).thenReturn(false);

        service.exportItem(ctx, item);

        verify(fileRepository).createDirIfRequired(itemDir);
        verify(fileRepository, atLeastOnce()).copy(any(Path.class), any(Path.class));
    }

    @Test
    void testCopyExportAndDelete() {
        Path export = Path.of("export.zip");
        OutputStream out = mock(OutputStream.class);

        service.copyExportAndDelete(export, out);

        verify(fileRepository).copy(export, out);
        verify(fileRepository).delete(export);
    }

    @Test
    void testCopyItemMediaFilesOptimizedWithModel() {
        List<String> images = new LinkedList<>();
        images.add("img.png");
        List<String> models = new LinkedList<>();
        models.add("model.glb");

        Item item = createItem("item4", images, models);
        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().optimizeSize(true).build())
                .build();

        Path itemDir = exportsDir.resolve("item4");
        when(fileRepository.exists(itemDir)).thenReturn(false);

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        service.exportItem(ctx, item);

        verify(fileRepository).copy(any(Path.class), argThat(p -> p.toString().endsWith("model.glb")), eq(StandardCopyOption.REPLACE_EXISTING));
        assertThat(item.getMediaContent().getModels()).containsExactly("model.glb");
        assertThat(item.getMediaContent().getImages()).isEmpty();
    }

    @Test
    void testCopyItemMediaFilesOptimizedWithImageOnly() {
        List<String> images = new LinkedList<>();
        images.add("img.png");
        List<String> models = new LinkedList<>();
        Item item = createItem("item5", images, models);
        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().optimizeSize(true).build())
                .build();

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        Path itemDir = exportsDir.resolve("item5");
        when(fileRepository.exists(itemDir)).thenReturn(false);

        service.exportItem(ctx, item);

        verify(fileRepository).copy(any(Path.class), argThat(p -> p.toString().endsWith("img.png")), eq(StandardCopyOption.REPLACE_EXISTING));
        assertThat(item.getMediaContent().getImages()).containsExactly("img.png");
        assertThat(item.getMediaContent().getModels()).isEmpty();
    }

}
