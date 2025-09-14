package com.arassec.artivact.adapter.in.rest.controller.item;

import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMediaCreationControllerTest {

    @Mock
    private CaptureItemImageUseCase captureImagesUseCase;

    @Mock
    private ManipulateItemImagesUseCase manipulateImagesUseCase;

    @Mock
    private ManageItemImagesUseCase manageItemImagesUseCase;

    @Mock
    private ManageItemModelsUseCase manageItemModelsUseCase;

    @Mock
    private CreateItemModelUseCase createItemModelUseCase;

    @Mock
    private EditItemModelUseCase editItemModelUseCase;

    @InjectMocks
    private ItemMediaCreationController controller;

    @Test
    void testCaptureImage() {
        CaptureImagesParams params = new CaptureImagesParams();
        params.setRemoveBackgrounds(true);

        when(captureImagesUseCase.captureImage("id-1", true)).thenReturn("result");

        String result = controller.captureImage("id-1", params);

        assertThat(result).isEqualTo("result");
        verify(captureImagesUseCase).captureImage("id-1", true);
    }

    @Test
    void testCaptureImages() {
        CaptureImagesParams params = new CaptureImagesParams();

        controller.captureImages("id-1", params);

        verify(captureImagesUseCase).captureImages("id-1", params);
    }

    @Test
    void testRemoveBackgrounds() {
        controller.removeBackgrounds("id-1", 5);
        verify(manipulateImagesUseCase).removeBackgrounds("id-1", 5);
    }

    @Test
    void testCreateImageSetFromDanglingImages() {
        controller.createImageSetFromDanglingImages("id-1");
        verify(manageItemImagesUseCase).createImageSetFromDanglingImages("id-1");
    }

    @Test
    void testCreateModelSet() {
        controller.createModelSet("id-1");
        verify(createItemModelUseCase).createModel("id-1");
    }

    @Test
    void testOpenModelEditor() {
        controller.openModelEditor("id-1", 3);
        verify(editItemModelUseCase).editModel("id-1", 3);
    }

    @Test
    void testGetModelSetFiles() {
        List<Asset> assets = List.of(new Asset());
        when(manageItemModelsUseCase.getModelSetFiles("id-1", 7)).thenReturn(assets);

        ResponseEntity<List<Asset>> response = controller.getModelSetFiles("id-1", 7);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(assets);
        verify(manageItemModelsUseCase).getModelSetFiles("id-1", 7);
    }

    @Test
    void testOpenImagesDir() {
        controller.openImagesDir("id-1");
        verify(manageItemImagesUseCase).openImagesDir("id-1");
    }

    @Test
    void testOpenModelsDir() {
        controller.openModelsDir("id-1");
        verify(manageItemModelsUseCase).openModelsDir("id-1");
    }

    @Test
    void testOpenModelDir() {
        controller.openModelDir("id-1", 2);
        verify(manageItemModelsUseCase).openModelDir("id-1", 2);
    }

    @Test
    void testTransferImage() {
        Asset image = new Asset();
        controller.transferImage("id-1", image);
        verify(manageItemImagesUseCase).transferImageToMedia("id-1", image);
    }

    @Test
    void testTransferModel() {
        Asset file = new Asset();
        controller.transferModel("id-1", 4, file);
        verify(manageItemModelsUseCase).transferModelToMedia("id-1", 4, file);
    }

    @Test
    void testDeleteImageSet() {
        controller.deleteImageSet("id-1", 6);
        verify(manageItemImagesUseCase).deleteImageSet("id-1", 6);
    }

    @Test
    void testDeleteModelSet() {
        controller.deleteModelSet("id-1", 8);
        verify(manageItemModelsUseCase).deleteModelSet("id-1", 8);
    }

    @Test
    void testToggleModelInput() {
        controller.toggleModelInput("id-1", 9);
        verify(manageItemModelsUseCase).toggleModelInput("id-1", 9);
    }

}
