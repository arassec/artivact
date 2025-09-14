package com.arassec.artivact.adapter.in.rest.controller.item;

import com.arassec.artivact.adapter.in.rest.model.ItemDetails;
import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ImportItemUseCase importItemUseCase;

    @Mock
    private CreateItemUseCase createItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private ManageItemImagesUseCase manageItemImagesUseCase;

    @Mock
    private ManageItemModelsUseCase manageItemModelsUseCase;

    @Mock
    private ExportItemUseCase exportItemUseCase;

    @Mock
    private DeleteItemUseCase deleteItemUseCase;

    @Mock
    private UploadItemUseCase uploadItemUseCase;

    @InjectMocks
    private ItemController itemController;

    @Test
    void testCreate() {
        Item item = new Item();
        item.setId("test-id");
        when(createItemUseCase.create()).thenReturn(item);
        when(saveItemUseCase.save(item)).thenReturn(item);

        ResponseEntity<String> result = itemController.create();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo("test-id");

        verify(createItemUseCase).create();
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testLoad() {
        Item item = new Item();
        item.setId("123");
        item.setVersion(1);
        item.setRestrictions(Set.of("restricted"));
        item.setTitle(new TranslatableString("Title"));
        item.setDescription(new TranslatableString("Description"));
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());

        when(loadItemUseCase.loadTranslatedRestricted("123")).thenReturn(item);

        ResponseEntity<?> response = itemController.load("123");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().toString()).contains("Title");

        verify(loadItemUseCase).loadTranslatedRestricted("123");
    }

    @Test
    void testSaveItem() {
        Item item = new Item();
        item.setId("123");
        item.setMediaContent(new MediaContent());
        item.setMediaCreationContent(new MediaCreationContent());

        ItemDetails details = ItemDetails.builder()
                .id("123")
                .title(new TranslatableString("Updated Title"))
                .description(new TranslatableString("Updated Description"))
                .images(List.of())
                .models(List.of())
                .creationImageSets(List.of())
                .creationModelSets(List.of())
                .restrictions(List.of())
                .build();

        when(loadItemUseCase.loadTranslatedRestricted("123")).thenReturn(item);
        when(saveItemUseCase.save(any(Item.class))).thenReturn(item);

        ResponseEntity<ItemDetails> response = itemController.save(details);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle().getValue()).isEqualTo("Updated Title");

        verify(saveItemUseCase).save(item);
    }

    @Test
    void testDelete() {
        itemController.delete("to-delete");
        verify(deleteItemUseCase).delete("to-delete");
    }

    @Test
    void testGetImage() {
        byte[] fakeImage = "img".getBytes();
        when(manageItemImagesUseCase.loadImage("123", "pic.png", ImageSize.ORIGINAL)).thenReturn(fakeImage);

        HttpEntity<byte[]> response = itemController.getImage("123", "pic.png", null);

        assertThat(response.getBody()).isEqualTo(fakeImage);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        verify(manageItemImagesUseCase).loadImage("123", "pic.png", ImageSize.ORIGINAL);
    }

    @Test
    void testGetModel() {
        byte[] fakeModel = "model".getBytes();
        when(manageItemModelsUseCase.loadModel("123", "test.glb")).thenReturn(fakeModel);

        HttpEntity<byte[]> response = itemController.getModel("123", "test.glb");

        assertThat(response.getBody()).isEqualTo(fakeModel);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("test.glb");
    }

    @Test
    void testUploadImageAddImage() {
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());
        itemController.uploadImage("123", file, false);
        verify(manageItemImagesUseCase).addImage("123", file);
    }

    @Test
    void testUploadImageUploadOnly() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new ByteArrayInputStream("img".getBytes()));
        itemController.uploadImage("123", file, true);
        verify(manageItemImagesUseCase).saveImage(eq("123"), eq("test.png"), any(), eq(false));
    }

    @Test
    void testUploadImageIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("broken.png");
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        assertThatThrownBy(() -> itemController.uploadImage("123", file, true))
                .isInstanceOf(ArtivactException.class);
    }

    @Test
    void testUploadModel() {
        MultipartFile file = new MockMultipartFile("file", "test.glb", "model/gltf-binary", "model".getBytes());
        itemController.uploadModel("123", file);
        verify(manageItemModelsUseCase).addModel("123", file);
    }

    @Test
    void testExportItem() {
        Path fakeExport = Path.of("fake.zip");
        when(exportItemUseCase.exportItem("123")).thenReturn(fakeExport);

        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseEntity<StreamingResponseBody> entity = itemController.exportItem(response, "123");

        assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(entity.getBody()).isNotNull();
        verify(exportItemUseCase).exportItem("123");
    }

    @Test
    void testUploadItemToRemoteInstance() {
        itemController.uploadItemToRemoteInstance("remote-1");
        verify(uploadItemUseCase).uploadItemToRemoteInstance("remote-1", true);
    }

    @Test
    void testImportItem() {
        MultipartFile file = new MockMultipartFile("file", "import.zip", "application/zip", "zipdata".getBytes());
        Path tmpDir = Path.of("tmp");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tmpDir);

        ResponseEntity<String> response = itemController.importItem(file);

        assertThat(response.getBody()).isEqualTo("Item imported.");
        verify(importItemUseCase).importItem(tmpDir.resolve("upload_import.zip"));
        verify(fileRepository).delete(tmpDir.resolve("upload_import.zip"));
    }

    @Test
    void testImportItemWithApiToken() {
        MultipartFile file = new MockMultipartFile("file", "token_import.zip", "application/zip", "zipdata".getBytes());
        Path tmpDir = Path.of("tmp");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tmpDir);

        ResponseEntity<String> response = itemController.importItemWithApiToken(file, "token-123");

        assertThat(response.getBody()).isEqualTo("Item synchronized.");
        verify(importItemUseCase).importItem(tmpDir.resolve("upload_token_import.zip"), "token-123");
        verify(fileRepository).delete(tmpDir.resolve("upload_token_import.zip"));
    }

}
