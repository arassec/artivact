package com.arassec.artivact.domain.exchange.exp;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ItemExporter}.
 */
@ExtendWith(MockitoExtension.class)
class ItemExporterTest {

    /**
     * Exporter under test.
     */
    @InjectMocks
    private ItemExporter itemExporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * The application's {@link FileRepository}.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * The application's project data provider.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * Tests exporting an item.
     */
    @Test
    @SneakyThrows
    void testExportItem() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        when(fileRepository.getDirFromId(any(Path.class), eq("123-abc"))).thenReturn(Path.of("item-dir"));

        Path exportDir = Path.of("export-dir");

        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(exportDir);
        exportContext.setExportConfiguration(ExportConfiguration.builder().build());

        Item item = new Item();
        item.setId("123-abc");
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));
        item.getMediaContent().getImages().add("image.jpg");
        item.getMediaContent().getModels().add("model.glb");

        itemExporter.exportItem(exportContext, item);

        verify(fileRepository).createDirIfRequired(Path.of("export-dir/123-abc"));
        verify(fileRepository).copy(Path.of("item-dir/images/image.jpg"), Path.of("export-dir/123-abc/image.jpg"));
        verify(fileRepository).copy(Path.of("item-dir/models/model.glb"), Path.of("export-dir/123-abc/model.glb"));

        verify(objectMapper).writeValue(Path.of("export-dir/123-abc/artivact.item.json").toFile(), item);
    }

    /**
     * Tests exporting an item which has already been exported by a different widget.
     */
    @Test
    @SneakyThrows
    void testExportItemAlreadyExported() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));

        Item item = new Item();
        item.setId("123-abc");

        when(fileRepository.exists(Path.of("export-dir/123-abc"))).thenReturn(true);

        itemExporter.exportItem(exportContext, item);

        verify(objectMapper, times(0)).writeValue(any(File.class), any(Item.class));
    }

    /**
     * Tests exporting an item with size optimization and existing model file.
     */
    @Test
    @SneakyThrows
    void testExportItemOptimizeSizeModel() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        when(fileRepository.getDirFromId(any(Path.class), eq("123-abc"))).thenReturn(Path.of("item-dir"));

        Path exportDir = Path.of("export-dir");

        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(exportDir);
        exportContext.setExportConfiguration(ExportConfiguration.builder()
                .optimizeSize(true)
                .build());

        Item item = new Item();
        item.setId("123-abc");
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));
        item.getMediaContent().getImages().add("image1.jpg");
        item.getMediaContent().getImages().add("image2.jpg");
        item.getMediaContent().getModels().add("model1.glb");
        item.getMediaContent().getModels().add("model2.glb");

        itemExporter.exportItem(exportContext, item);

        // Model before image, and only the first one is used:
        verify(fileRepository).copy(Path.of("item-dir/models/model1.glb"), Path.of("export-dir/123-abc/model1.glb"));
        verify(fileRepository, times(0))
                .copy(Path.of("item-dir/images/image1.jpg"), Path.of("export-dir/123-abc/image1.jpg"));
        verify(fileRepository, times(0))
                .copy(Path.of("item-dir/images/image2.jpg"), Path.of("export-dir/123-abc/image2.jpg"));
        verify(fileRepository, times(0))
                .copy(Path.of("item-dir/models/model2.glb"), Path.of("export-dir/123-abc/model2.glb"));
    }

    /**
     * Tests exporting an item with size optimization and only images available.
     */
    @Test
    @SneakyThrows
    void testExportItemOptimizeSizeImage() {
        Path root = Path.of("root");
        when(projectDataProvider.getProjectRoot()).thenReturn(root);

        when(fileRepository.getDirFromId(any(Path.class), eq("123-abc"))).thenReturn(Path.of("item-dir"));

        Path exportDir = Path.of("export-dir");

        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(exportDir);
        exportContext.setExportConfiguration(ExportConfiguration.builder()
                .optimizeSize(true)
                .build());

        Item item = new Item();
        item.setId("123-abc");
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));
        item.getMediaContent().getImages().add("image1.jpg");
        item.getMediaContent().getImages().add("image2.jpg");

        itemExporter.exportItem(exportContext, item);

        // Model before image, and only the first one is used:
        verify(fileRepository).copy(Path.of("item-dir/images/image1.jpg"), Path.of("export-dir/123-abc/image1.jpg"));
        verify(fileRepository, times(0))
                .copy(Path.of("item-dir/images/image2.jpg"), Path.of("export-dir/123-abc/image2.jpg"));
    }

}
