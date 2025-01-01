package com.arassec.artivact.domain.exchange.importer;

import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Tests the {link ItemImporter}.
 */
@ExtendWith(MockitoExtension.class)
class ItemImporterTest {

    /**
     * Importer under test.
     */
    @InjectMocks
    private ItemImporter itemImporter;

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
     * Service for item handling.
     */
    @Mock
    private ItemService itemService;

    /**
     * Import context for testing.
     */
    private final ImportContext importContext = ImportContext.builder()
            .importDir(Path.of("import-dir"))
            .build();

    /**
     * Tests importing an item.
     */
    @Test
    @SneakyThrows
    void testImportItem() {
        Item item = new Item();
        item.setId("item-id");
        item.getMediaContent().getImages().add("image.jpg");
        item.getMediaContent().getModels().add("model.glb");

        when(fileRepository.read(Path.of("import-dir/item-id/artivact.item.json"))).thenReturn("item-json");
        when(objectMapper.readValue("item-json", Item.class)).thenReturn(item);

        InputStream imageInputStream = mock(InputStream.class);
        when(fileRepository.readStream(Path.of("import-dir/item-id/image.jpg"))).thenReturn(imageInputStream);

        InputStream modelInputStream = mock(InputStream.class);
        when(fileRepository.readStream(Path.of("import-dir/item-id/model.glb"))).thenReturn(modelInputStream);

        itemImporter.importItem(importContext, item.getId());

        verify(itemService).saveImage("item-id", "image.jpg", imageInputStream, true);
        verify(itemService).saveModel("item-id", "model.glb", modelInputStream, true);

        verify(itemService).save(item);
    }

}
