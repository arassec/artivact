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
import org.springframework.http.HttpEntity;
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
	void captureImageReturnsCreatedImageName() {
		CaptureImagesParams params = CaptureImagesParams.builder().numPhotos(1).build();
		when(captureImagesUseCase.captureImage("item-1", params)).thenReturn("captured.png");

		String result = controller.captureImage("item-1", params);

		assertThat(result).isEqualTo("captured.png");
	}

	@Test
	void captureImagesStartsCapturingForProvidedItem() {
		CaptureImagesParams params = CaptureImagesParams.builder().numPhotos(12).useTurnTable(true).build();

		controller.captureImages("item-1", params);

		verify(captureImagesUseCase).captureImages("item-1", params);
	}

	@Test
	void removeBackgroundsUsesConfiguredManipulatorAndImageSet() {
		controller.removeBackgrounds("item-1", "config-1", 3);

		verify(manipulateImagesUseCase).removeBackgrounds("item-1", "config-1", 3);
	}

	@Test
	void createImageSetFromDanglingImagesDelegatesToUseCase() {
		controller.createImageSetFromDanglingImages("item-1");

		verify(manageItemImagesUseCase).createImageSetFromDanglingImages("item-1");
	}

	@Test
	void createModelSetStartsModelCreationForProvidedItem() {
		CreateModelParams params = CreateModelParams.builder().modelCreatorPeripheralConfigId("creator-1").build();

		controller.createModelSet("item-1", params);

		verify(createItemModelUseCase).createModel("item-1", params);
	}

	@Test
	void openModelEditorUsesRequestedModelSetAndEditor() {
		controller.openModelEditor("item-1", 2, "editor-1");

		verify(editItemModelUseCase).editModel("item-1", "editor-1", 2);
	}

	@Test
	void getModelSetFilesReturnsAssetsOfRequestedModelSet() {
		List<Asset> assets = List.of(
				Asset.builder().fileName("model.glb").url("/api/item/item-1/model/model.glb").transferable(true).build()
		);
		when(manageItemModelsUseCase.getModelSetFiles("item-1", 1)).thenReturn(assets);

		ResponseEntity<List<Asset>> result = controller.getModelSetFiles("item-1", 1);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isEqualTo(assets);
	}

	@Test
	void getModelSetFileReturnsGlbContentWithBinaryGltfMediaType() {
		byte[] data = "glb".getBytes();
		when(manageItemModelsUseCase.loadModelSetFile("item-1", 1, "MODEL.GLB")).thenReturn(data);

		HttpEntity<byte[]> result = controller.getModelSetFile("item-1", 1, "MODEL.GLB");

		assertThat(result.getBody()).isEqualTo(data);
		assertThat(result.getHeaders().getContentDisposition().getFilename()).isEqualTo("MODEL.GLB");
		assertThat(result.getHeaders().getContentType()).hasToString("model/gltf-binary");
	}

	@Test
	void getModelSetFileReturnsGltfContentWithJsonMediaType() {
		byte[] data = "gltf".getBytes();
		when(manageItemModelsUseCase.loadModelSetFile("item-1", 1, "scene.gltf")).thenReturn(data);

		HttpEntity<byte[]> result = controller.getModelSetFile("item-1", 1, "scene.gltf");

		assertThat(result.getBody()).isEqualTo(data);
		assertThat(result.getHeaders().getContentType()).hasToString("model/gltf+json");
	}

	@Test
	void getModelSetFileReturnsObjContentAsPlainText() {
		byte[] data = "obj".getBytes();
		when(manageItemModelsUseCase.loadModelSetFile("item-1", 1, "mesh.obj")).thenReturn(data);

		HttpEntity<byte[]> result = controller.getModelSetFile("item-1", 1, "mesh.obj");

		assertThat(result.getBody()).isEqualTo(data);
		assertThat(result.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.TEXT_PLAIN);
	}

	@Test
	void getModelSetFileUsesGuessedContentTypeForKnownFileExtensions() {
		byte[] data = "png".getBytes();
		when(manageItemModelsUseCase.loadModelSetFile("item-1", 1, "preview.png")).thenReturn(data);

		HttpEntity<byte[]> result = controller.getModelSetFile("item-1", 1, "preview.png");

		assertThat(result.getBody()).isEqualTo(data);
		assertThat(result.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.IMAGE_PNG);
	}

	@Test
	void getModelSetFileFallsBackToOctetStreamForUnknownFileExtensions() {
		byte[] data = "unknown".getBytes();
		when(manageItemModelsUseCase.loadModelSetFile("item-1", 1, "archive.unknownext")).thenReturn(data);

		HttpEntity<byte[]> result = controller.getModelSetFile("item-1", 1, "archive.unknownext");

		assertThat(result.getBody()).isEqualTo(data);
		assertThat(result.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
	}

	@Test
	void openImagesDirDelegatesToUseCase() {
		controller.openImagesDir("item-1");

		verify(manageItemImagesUseCase).openImagesDir("item-1");
	}

	@Test
	void openModelsDirDelegatesToUseCase() {
		controller.openModelsDir("item-1");

		verify(manageItemModelsUseCase).openModelsDir("item-1");
	}

	@Test
	void openModelDirDelegatesToUseCase() {
		controller.openModelDir("item-1", 4);

		verify(manageItemModelsUseCase).openModelDir("item-1", 4);
	}

	@Test
	void transferImageDelegatesProvidedAssetToUseCase() {
		Asset image = Asset.builder().fileName("image.png").url("/api/item/item-1/image/image.png").build();

		controller.transferImage("item-1", image);

		verify(manageItemImagesUseCase).transferImageToMedia("item-1", image);
	}

	@Test
	void transferImagesDelegatesRequestedImageSetToUseCase() {
		controller.transferImages("item-1", 5);

		verify(manageItemImagesUseCase).transferImagesToMedia("item-1", 5);
	}

	@Test
	void hasTransferableModelReturnsUseCaseResult() {
		when(manageItemModelsUseCase.hasTransferableModel("item-1", 2)).thenReturn(true);

		boolean result = controller.hasTransferableModel("item-1", 2);

		assertThat(result).isTrue();
	}

	@Test
	void transferModelDelegatesToUseCase() {
		controller.transferModel("item-1", 2);

		verify(manageItemModelsUseCase).transferModelToMedia("item-1", 2);
	}

	@Test
	void deleteImageSetDelegatesToUseCase() {
		controller.deleteImageSet("item-1", 6);

		verify(manageItemImagesUseCase).deleteImageSet("item-1", 6);
	}

	@Test
	void deleteModelSetDelegatesToUseCase() {
		controller.deleteModelSet("item-1", 7);

		verify(manageItemModelsUseCase).deleteModelSet("item-1", 7);
	}

	@Test
	void toggleModelInputDelegatesToUseCase() {
		controller.toggleModelInput("item-1", 8);

		verify(manageItemModelsUseCase).toggleModelInput("item-1", 8);
	}

}
