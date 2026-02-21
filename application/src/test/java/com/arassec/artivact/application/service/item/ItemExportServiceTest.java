package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

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
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LoadItemUseCase loadItemUseCase;

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

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("root"));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        when(loadItemUseCase.loadTranslated("item1")).thenReturn(item);

        Path result = service.exportItem("item1");

        assertThat(result.toString()).endsWith("item1.artivact.collection.zip");
        verify(loadItemUseCase).loadTranslated("item1");

        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), any(Item.class));
        assertThat(argCap.getValue().toString()).endsWith("artivact.item.json");
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
    @SneakyThrows
    void testExportItemDirectlyWithItemObject() {
        // Given
        Item item = createItem("item-direct", new LinkedList<>(List.of("img.png")), new LinkedList<>(List.of("model.glb")));

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("root"));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));

        // When
        Path result = service.exportItem(item);

        // Then
        assertThat(result.toString()).endsWith("item-direct.artivact.collection.zip");
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());
    }

    @Test
    void testExportItemStandardCopiesAllMediaFiles() {
        // Given
        List<String> images = new LinkedList<>(List.of("img1.png", "img2.png"));
        List<String> models = new LinkedList<>(List.of("model1.glb", "model2.glb"));
        Item item = createItem("item-std", images, models);

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().xrExport(false).build())
                .build();

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));
        when(fileRepository.exists(any(Path.class))).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then - all images and models should be copied (2 images + 2 models = 4 copy calls + dirs)
        verify(fileRepository, times(4)).copy(any(Path.class), any(Path.class));
        assertThat(item.getMediaContent().getImages()).containsExactly("img1.png", "img2.png");
        assertThat(item.getMediaContent().getModels()).containsExactly("model1.glb", "model2.glb");
        assertThat(item.getMediaCreationContent()).isNull();
    }

    @Test
    void testExportItemClearsTranslatedValues() {
        // Given
        Item item = createItem("item-trans", new LinkedList<>(), new LinkedList<>());
        item.getTitle().setTranslatedValue("translated-title");
        item.getDescription().setTranslatedValue("translated-desc");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));
        when(fileRepository.exists(any(Path.class))).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then
        assertThat(item.getTitle().getTranslatedValue()).isNull();
        assertThat(item.getDescription().getTranslatedValue()).isNull();
    }

    @Test
    void testXrExportWithNoMediaFiles() {
        // Given
        List<String> images = new LinkedList<>();
        List<String> models = new LinkedList<>();
        Item item = createItem("item-empty-xr", images, models);

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().xrExport(true).build())
                .build();

        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("."));
        when(fileRepository.exists(any(Path.class))).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then - no media files copied with REPLACE_EXISTING
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class), any(StandardCopyOption.class));
        assertThat(item.getMediaContent().getImages()).isEmpty();
        assertThat(item.getMediaContent().getModels()).isEmpty();
        assertThat(item.getMediaCreationContent()).isNull();
    }

    @Test
    void testExportItemSkipsAlreadyExportedItem() {
        // Given
        Item item = createItem("item-exists", new LinkedList<>(), new LinkedList<>());

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(new ExportConfiguration())
                .build();

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("existing-dir"));
        when(fileRepository.exists(Path.of("existing-dir"))).thenReturn(true);

        // When
        service.exportItem(ctx, item);

        // Then - no further interactions beyond exists check
        verify(fileRepository, never()).createDirIfRequired(any());
        verify(fileRepository, never()).copy(any(Path.class), any(Path.class));
        verify(jsonMapper, never()).writeValue(any(File.class), any());
    }

    @Test
    void testXrExportWithModelsExportsFirstModelOnly() {
        // Given
        List<String> images = new LinkedList<>(List.of("img1.png", "img2.png"));
        List<String> models = new LinkedList<>(List.of("model1.glb", "model2.glb"));
        Item item = createItem("item-xr-models", images, models);

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().xrExport(true).build())
                .build();

        Path itemExportDir = Path.of("item-export-dir");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(itemExportDir);
        when(fileRepository.exists(itemExportDir)).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then - only first model is copied with REPLACE_EXISTING
        verify(fileRepository).copy(
                any(Path.class),
                eq(itemExportDir.resolve("model1.glb")),
                eq(StandardCopyOption.REPLACE_EXISTING)
        );
        assertThat(item.getMediaContent().getModels()).containsExactly("model1.glb");
        assertThat(item.getMediaContent().getImages()).isEmpty();
        assertThat(item.getMediaCreationContent()).isNull();
    }

    @Test
    void testXrExportWithOnlyImagesExportsFirstImageOnly() {
        // Given
        List<String> images = new LinkedList<>(List.of("img1.png", "img2.png"));
        List<String> models = new LinkedList<>();
        Item item = createItem("item-xr-images", images, models);

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().xrExport(true).build())
                .build();

        Path itemExportDir = Path.of("item-export-dir");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(itemExportDir);
        when(fileRepository.exists(itemExportDir)).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then - only first image is copied with REPLACE_EXISTING
        verify(fileRepository).copy(
                any(Path.class),
                eq(itemExportDir.resolve("img1.png")),
                eq(StandardCopyOption.REPLACE_EXISTING)
        );
        assertThat(item.getMediaContent().getImages()).containsExactly("img1.png");
        assertThat(item.getMediaContent().getModels()).isEmpty();
        assertThat(item.getMediaCreationContent()).isNull();
    }

    @Test
    void testExportItemCreatesRequiredDirectories() {
        // Given
        Item item = createItem("item-dirs", new LinkedList<>(), new LinkedList<>());

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().xrExport(false).build())
                .build();

        Path itemExportDir = Path.of("item-export-dir");
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(itemExportDir);
        when(fileRepository.exists(itemExportDir)).thenReturn(false);

        // When
        service.exportItem(ctx, item);

        // Then - item dir + images dir + models dir = 3 createDirIfRequired calls
        verify(fileRepository).createDirIfRequired(itemExportDir);
        verify(fileRepository).createDirIfRequired(itemExportDir.resolve("images"));
        verify(fileRepository).createDirIfRequired(itemExportDir.resolve("models"));
    }

}
