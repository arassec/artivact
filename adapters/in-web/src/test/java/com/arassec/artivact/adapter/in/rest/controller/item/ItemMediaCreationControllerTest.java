package com.arassec.artivact.adapter.in.rest.controller.item;

import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.media.CreateModelParams;
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

    @InjectMocks
    private ItemMediaCreationController controller;

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

    @Test
    void captureImageCapturesSingleImage() {
        String itemId = "item-123";
        CaptureImagesParams params = CaptureImagesParams.builder()
                .cameraPeripheralConfigId("camera-1")
                .build();

        when(captureImagesUseCase.captureImage(itemId, params)).thenReturn("captured.jpg");

        String result = controller.captureImage(itemId, params);

        assertThat(result).isEqualTo("captured.jpg");
        verify(captureImagesUseCase).captureImage(itemId, params);
    }

    @Test
    void captureImagesCapturesMultipleImages() {
        String itemId = "item-123";
        CaptureImagesParams params = CaptureImagesParams.builder()
                .cameraPeripheralConfigId("camera-1")
                .numPhotos(10)
                .build();

        controller.captureImages(itemId, params);

        verify(captureImagesUseCase).captureImages(itemId, params);
    }

    @Test
    void removeBackgroundsCallsManipulateImagesUseCase() {
        String itemId = "item-123";
        String configId = "bg-remover-1";
        int imageSetIndex = 0;

        controller.removeBackgrounds(itemId, configId, imageSetIndex);

        verify(manipulateImagesUseCase).removeBackgrounds(itemId, configId, imageSetIndex);
    }

    @Test
    void createImageSetFromDanglingImagesCallsManageItemImagesUseCase() {
        String itemId = "item-123";

        controller.createImageSetFromDanglingImages(itemId);

        verify(manageItemImagesUseCase).createImageSetFromDanglingImages(itemId);
    }

    @Test
    void createModelSetCallsCreateItemModelUseCase() {
        String itemId = "item-123";
        CreateModelParams params = CreateModelParams.builder()
                .modelCreatorPeripheralConfigId("creator-1")
                .build();

        controller.createModelSet(itemId, params);

        verify(createItemModelUseCase).createModel(itemId, params);
    }

    @Test
    void openModelEditorCallsEditItemModelUseCase() {
        String itemId = "item-123";
        int modelSetIndex = 0;
        String configId = "editor-1";

        controller.openModelEditor(itemId, modelSetIndex, configId);

        verify(editItemModelUseCase).editModel(itemId, configId, modelSetIndex);
    }

    @Test
    void getModelSetFilesReturnsAssetList() {
        String itemId = "item-123";
        int modelSetIndex = 0;
        List<Asset> assets = List.of(
                Asset.builder().fileName("model.glb").build(),
                Asset.builder().fileName("texture.jpg").build()
        );

        when(manageItemModelsUseCase.getModelSetFiles(itemId, modelSetIndex)).thenReturn(assets);

        ResponseEntity<List<Asset>> response = controller.getModelSetFiles(itemId, modelSetIndex);

        assertThat(response.getBody()).isEqualTo(assets);
    }

    @Test
    void openImagesDirCallsManageItemImagesUseCase() {
        String itemId = "item-123";

        controller.openImagesDir(itemId);

        verify(manageItemImagesUseCase).openImagesDir(itemId);
    }

    @Test
    void openModelsDirCallsManageItemModelsUseCase() {
        String itemId = "item-123";

        controller.openModelsDir(itemId);

        verify(manageItemModelsUseCase).openModelsDir(itemId);
    }

    @Test
    void openModelDirCallsManageItemModelsUseCase() {
        String itemId = "item-123";
        int modelSetIndex = 0;

        controller.openModelDir(itemId, modelSetIndex);

        verify(manageItemModelsUseCase).openModelDir(itemId, modelSetIndex);
    }

    @Test
    void transferImageCallsManageItemImagesUseCase() {
        String itemId = "item-123";
        Asset image = Asset.builder().fileName("001.jpg").build();

        controller.transferImage(itemId, image);

        verify(manageItemImagesUseCase).transferImageToMedia(itemId, image);
    }

    @Test
    void transferModelCallsManageItemModelsUseCase() {
        String itemId = "item-123";
        int modelSetIndex = 0;
        Asset file = Asset.builder().fileName("model.glb").build();

        controller.transferModel(itemId, modelSetIndex, file);

        verify(manageItemModelsUseCase).transferModelToMedia(itemId, modelSetIndex, file);
    }

    @Test
    void deleteImageSetCallsManageItemImagesUseCase() {
        String itemId = "item-123";
        int imageSetIndex = 0;

        controller.deleteImageSet(itemId, imageSetIndex);

        verify(manageItemImagesUseCase).deleteImageSet(itemId, imageSetIndex);
    }

    @Test
    void deleteModelSetCallsManageItemModelsUseCase() {
        String itemId = "item-123";
        int modelSetIndex = 0;

        controller.deleteModelSet(itemId, modelSetIndex);

        verify(manageItemModelsUseCase).deleteModelSet(itemId, modelSetIndex);
    }

    @Test
    void toggleModelInputCallsManageItemModelsUseCase() {
        String itemId = "item-123";
        int imageSetIndex = 0;

        controller.toggleModelInput(itemId, imageSetIndex);

        verify(manageItemModelsUseCase).toggleModelInput(itemId, imageSetIndex);
    }
}
