package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.item.Asset;
import com.arassec.artivact.domain.creator.CapturePhotosParams;
import com.arassec.artivact.domain.service.MediaCreationService;
import com.arassec.artivact.web.model.OperationProgress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ItemMediaCreationController}.
 */
@ExtendWith(MockitoExtension.class)
class ItemMediaCreationControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ItemMediaCreationController controller;

    /**
     * Service mock.
     */
    @Mock
    private MediaCreationService mediaCreationService;

    /**
     * Test progress expected from service calls.
     */
    private final OperationProgress expectedOperationProgress = OperationProgress.builder()
            .key("Progress.ItemMediaCreationControllerTest.test")
            .currentAmount(0)
            .targetAmount(0)
            .build();

    /**
     * Tests capturing photos.
     */
    @Test
    void testCapturePhotos() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ItemMediaCreationControllerTest.class, "test");
        when(mediaCreationService.getProgressMonitor()).thenReturn(progressMonitor);

        CapturePhotosParams params = new CapturePhotosParams();

        ResponseEntity<OperationProgress> responseEntity = controller.capturePhotos("item-id", params);

        verify(mediaCreationService, times(1)).capturePhotos("item-id", params);
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests removing backgrounds from image-set images.
     */
    @Test
    void testRemoveBackgrounds() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ItemMediaCreationControllerTest.class, "test");
        when(mediaCreationService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.removeBackgrounds("item-id", 0);

        verify(mediaCreationService, times(1)).removeBackgrounds("item-id", 0);
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests creating an image-set from dangling images.
     */
    @Test
    void testCreateImageSetFromDanglingImages() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ItemMediaCreationControllerTest.class, "test");
        when(mediaCreationService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.createImageSetFromDanglingImages("item-id");

        verify(mediaCreationService, times(1)).createImageSetFromDanglingImages("item-id");
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests creating a model set.
     */
    @Test
    void testCreateModelSet() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ItemMediaCreationControllerTest.class, "test");
        when(mediaCreationService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.createModelSet("item-id");

        verify(mediaCreationService, times(1)).createModel("item-id");
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests opening a model editor.
     */
    @Test
    void testOpenModelEditor() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ItemMediaCreationControllerTest.class, "test");
        when(mediaCreationService.getProgressMonitor()).thenReturn(progressMonitor);

        ResponseEntity<OperationProgress> responseEntity = controller.openModelEditor("item-id", 0);

        verify(mediaCreationService, times(1)).editModel("item-id", 0);
        assertEquals(expectedOperationProgress, responseEntity.getBody());
    }

    /**
     * Tests listing the files in a model-set.
     */
    @Test
    void testGetModelSetFiles() {
        when(mediaCreationService.getModelSetFiles("item-id", 0)).thenReturn(List.of(
                Asset.builder().fileName("tst1").build(),
                Asset.builder().fileName("tst2").build()
        ));

        ResponseEntity<List<Asset>> responseEntity = controller.getModelSetFiles("item-id", 0);

        List<Asset> responseBody = responseEntity.getBody();

        assertNotNull(responseBody);
        assertEquals(2, responseBody.size());
        assertEquals("tst1", responseBody.get(0).getFileName());
        assertEquals("tst2", responseBody.get(1).getFileName());
    }

    /**
     * Test opening an image directory.
     */
    @Test
    void testOpenImagesDir() {
        controller.openImagesDir("item-id");
        verify(mediaCreationService, times(1)).openImagesDir("item-id");
    }

    /**
     * Tests opening a model directory.
     */
    @Test
    void testOpenModelDir() {
        controller.openModelsDir("item-id");
        verify(mediaCreationService, times(1)).openModelsDir("item-id");
    }

    /**
     * Tests transferring an image.
     */
    @Test
    void testTransferImage() {
        Asset file = Asset.builder().fileName("tst1").build();
        controller.transferImage("item-id", file);
        verify(mediaCreationService, times(1)).transferImageToMedia("item-id", file);
    }

    /**
     * Tests transferring a model.
     */
    @Test
    void testTransferModel() {
        Asset file = Asset.builder().fileName("tst1").build();
        controller.transferModel("item-id", 0, file);
        verify(mediaCreationService, times(1)).transferModelToMedia("item-id", 0, file);
    }

    /**
     * Tests deleting an image-set.
     */
    @Test
    void testDeleteImageSet() {
        controller.deleteImageSet("item-id", 0);
        verify(mediaCreationService, times(1)).deleteImageSet("item-id", 0);
    }

    /**
     * Tests deleting a model-set.
     */
    @Test
    void testDeleteModelSet() {
        controller.deleteModelSet("item-id", 0);
        verify(mediaCreationService, times(1)).deleteModelSet("item-id", 0);
    }

    /**
     * Tests toggling model input.
     */
    @Test
    void testToggleModelInput() {
        controller.toggleModelInput("item-id", 0);
        verify(mediaCreationService, times(1)).toggleModelInput("item-id", 0);
    }

}
