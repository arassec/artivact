package com.arassec.artivact.web.controller;


import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.item.CreationImageSet;
import com.arassec.artivact.core.model.item.CreationModelSet;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ItemService;
import com.arassec.artivact.web.model.ItemDetails;
import com.arassec.artivact.web.model.OperationProgress;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ItemController}.
 */
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ItemController controller;

    /**
     * Service mock.
     */
    @Mock
    private ItemService itemService;

    /**
     * Mock of the {@link ProjectDataProvider}.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * Tests creating an item.
     */
    @Test
    void testCreate() {
        Item item = new Item();
        item.setId("item-id");

        when(itemService.create()).thenReturn(item);
        when(itemService.save(item)).thenReturn(item);

        ResponseEntity<String> response = controller.create();

        assertEquals("item-id", response.getBody());
    }

    /**
     * Tests loading an item.
     */
    @Test
    void testLoad() {
        when(projectDataProvider.getProjectRoot()).thenReturn(Path.of(""));

        Item item = new Item();
        item.setId("item-id");
        item.setVersion(23);
        item.setRestrictions(Set.of("restriction"));
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));
        item.getMediaContent().getImages().add("image");
        item.getMediaContent().getModels().add("model");
        item.getMediaCreationContent().getImageSets().add(CreationImageSet.builder().build());
        item.getMediaCreationContent().getModelSets().add(CreationModelSet.builder().directory("modelDir").build());
        item.setProperties(Map.of("propertyKey", new TranslatableString("propertyValue")));
        item.setTags(List.of(new Tag("url", true)));

        assertThrows(IllegalArgumentException.class, () -> controller.load("item-id"));

        when(itemService.loadTranslatedRestricted("item-id")).thenReturn(item);

        ResponseEntity<ItemDetails> response = controller.load("item-id");

        ItemDetails itemDetails = response.getBody();

        assertNotNull(itemDetails);

        assertEquals("item-id", itemDetails.getId());
        assertEquals(23, itemDetails.getVersion());
        assertEquals("restriction", itemDetails.getRestrictions().getFirst());
        assertEquals("title", itemDetails.getTitle().getValue());
        assertEquals("description", itemDetails.getDescription().getValue());
        assertEquals("image", itemDetails.getImages().getFirst().getFileName());
        assertEquals("model", itemDetails.getModels().getFirst().getFileName());
        assertEquals(1, itemDetails.getCreationImageSets().size());
        assertEquals(1, itemDetails.getCreationModelSets().size());
        assertEquals("propertyValue", itemDetails.getProperties().get("propertyKey").getValue());
        assertEquals("url", itemDetails.getTags().getFirst().getUrl());
    }

    /**
     * Tests saving an item.
     */
    @Test
    void testSave() {
        Item item = new Item();
        item.setId("item-id");

        when(itemService.loadTranslatedRestricted("item-id")).thenReturn(item);

        when(itemService.save(item)).thenReturn(item);

        ResponseEntity<ItemDetails> response = controller.save(ItemDetails.builder()
                .id("item-id")
                .images(List.of())
                .creationImageSets(List.of())
                .models(List.of())
                .creationModelSets(List.of())
                .restrictions(List.of("restriction"))
                .build());

        ItemDetails itemDetails = response.getBody();

        assertNotNull(itemDetails);
        assertEquals("item-id", itemDetails.getId());
        assertEquals("restriction", itemDetails.getRestrictions().getFirst());
    }

    /**
     * Tests saving an unknown item fails save.
     */
    @Test
    void testSaveFailsafe() {
        ItemDetails itemdetails = new ItemDetails();
        assertThrows(IllegalArgumentException.class, () -> controller.save(itemdetails));
    }

    /**
     * Tests deleting an item.
     */
    @Test
    void testDeleteItem() {
        controller.deleteItem("item-id");
        verify(itemService, times(1)).delete("item-id");
    }

    /**
     * Tests getting an item's image.
     */
    @Test
    void testGetImage() {
        when(itemService.loadImage("item-id", "image.jpg", ImageSize.ORIGINAL))
                .thenReturn("imagecontent".getBytes());

        HttpEntity<byte[]> image = controller.getImage("item-id", "image.jpg", null);

        assertEquals(MediaType.IMAGE_JPEG_VALUE, image.getHeaders().getFirst("Content-Type"));

        byte[] imagecontent = image.getBody();
        assertNotNull(imagecontent);
        assertEquals("imagecontent", new String(imagecontent, Charset.defaultCharset()));
    }

    /**
     * Tests getting an item's model.
     */
    @Test
    void testGetModel() {
        when(itemService.loadModel("item-id", "model.glb")).thenReturn("modelcontent".getBytes());

        HttpEntity<byte[]> model = controller.getModel("item-id", "model.glb");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, model.getHeaders().getFirst("Content-Type"));
        assertEquals("inline; filename=\"model.glb\"", model.getHeaders().getFirst("Content-Disposition"));

        byte[] modelcontent = model.getBody();
        assertNotNull(modelcontent);
        assertEquals("modelcontent", new String(modelcontent, Charset.defaultCharset()));

    }

    /**
     * Tests uploading an image.
     */
    @Test
    void testUploadImage() {
        MultipartFile multipartFileMock = mock(MultipartFile.class);

        when(multipartFileMock.getOriginalFilename()).thenReturn("image.jpg");

        controller.uploadImage("item-id", multipartFileMock, true);
        verify(itemService, times(1)).saveImage("item-id", "image.jpg", null, false);

        controller.uploadImage("item-id", multipartFileMock, false);
        verify(itemService, times(1)).addImage("item-id", multipartFileMock);
    }

    /**
     * Tests uploading a model.
     */
    @Test
    void testUploadModel() {
        MultipartFile multipartFileMock = mock(MultipartFile.class);

        controller.uploadModel("item-id", multipartFileMock);

        verify(itemService, times(1)).addModel("item-id", multipartFileMock);
    }

    /**
     * Tests exporting an item.
     */
    @Test
    void testExportItem() {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        Path pathMock = mock(Path.class);
        when(itemService.exportItem("123-ABC")).thenReturn(pathMock);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                controller.exportItem(httpServletResponse, "123-ABC");

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType("application/zip");
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");

        assertNotNull(streamingResponseBodyResponseEntity.getBody());
    }

    /**
     * Tests the UI API for syncing items.
     */
    @Test
    void testUploadItemToRemoteInstance() {
        controller.uploadItemToRemoteInstance("123-abc");
        verify(itemService, times(1)).uploadItemToRemoteInstance("123-abc", true);
    }

    /**
     * Tests getting the progress of a long-running operation.
     */
    @Test
    void testGetProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(this.getClass(), "test");
        progressMonitor.updateProgress(10, 25);
        progressMonitor.updateProgress("error", new Exception());

        when(itemService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> progress = controller.getProgress();

        assertNotNull(progress.getBody());
        assertEquals("Progress.ItemControllerTest.error", progress.getBody().getKey());
        assertEquals(10, progress.getBody().getCurrentAmount());
        assertEquals(25, progress.getBody().getTargetAmount());
        assertNotNull(progress.getBody().getError());
    }

}
