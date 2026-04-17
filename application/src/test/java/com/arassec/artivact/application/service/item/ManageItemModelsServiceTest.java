package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageItemModelsServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @Mock
    private SaveItemUseCase saveItemUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @InjectMocks
    private ManageItemModelsService service;

    @Test
    void testLoadModelReturnsBytes() {
        Path path = Path.of("items/123/456/123456/models/test.glb");
        when(fileRepository.readBytes(path)).thenReturn(new byte[]{1, 2});
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));

        byte[] result = service.loadModel("123456", "test.glb");

        assertThat(result).containsExactly(1, 2);
    }

    @Test
    void testAddModelHappyPath() throws Exception {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        MultipartFile file = mock(MultipartFile.class);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(file.getOriginalFilename()).thenReturn("model.glb");
        when(file.getInputStream()).thenReturn(mock(InputStream.class));

        when(fileRepository.saveFile(any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn("model.glb");
        when(item.getMediaContent().getModels()).thenReturn(new LinkedList<>());

        service.addModel("id", file);

        verify(saveItemUseCase).save(item);
    }

    @Test
    void testAddModelThrowsIfItemNotFound() {
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(null);
        MultipartFile file = mock(MultipartFile.class);

        assertThatThrownBy(() -> service.addModel("id", file))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No item found");
    }

    @Test
    void testAddModelThrowsOnIOException() throws Exception {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        MultipartFile file = mock(MultipartFile.class);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(file.getOriginalFilename()).thenReturn("model.glb");
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        assertThatThrownBy(() -> service.addModel("id", file))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not add model!");
    }

    @Test
    void testSaveModelDelegatesToFileRepository() {
        InputStream data = mock(InputStream.class);

        service.saveModel("id", "f.glb", data, true);

        verify(fileRepository).saveFile(any(), eq("id"), eq("f.glb"), eq(data), any(), isNull(), eq(true));
    }

    @Test
    void testOpenModelsDirDelegatesToFileRepository() {
        Path path = Path.of("models");
        when(useProjectDirsUseCase.getModelsDir("id")).thenReturn(path);

        service.openModelsDir("id");

        verify(fileRepository).openDirInOs(path);
    }

    @Test
    void testOpenModelDirDelegatesToFileRepository() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        CreationModelSet modelSet = CreationModelSet.builder().directory("dir").build();
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("root"));

        service.openModelDir("id", 0);

        verify(fileRepository).openDirInOs(Path.of("root").resolve("dir"));
    }

    @Test
    void testGetModelSetFilesReturnsAssets() {
        Path dir = Path.of("models-dir");

        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        CreationModelSet modelSet = CreationModelSet.builder().directory(dir.toString()).build();
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of(""));
        when(fileRepository.list(dir)).thenReturn(List.of(dir.resolve("a.glb"), dir.resolve("b.png")));

        List<Asset> assets = service.getModelSetFiles("id", 0);

        assertThat(assets).hasSize(2);
        assertThat(assets.getFirst().getUrl()).isNotEmpty();
    }

    @Test
    void testLoadModelSetFileReturnsFileContents() {
        Path dir = Path.of("models-dir");
        Path modelFile = dir.resolve("model.glb");

        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        CreationModelSet modelSet = CreationModelSet.builder().directory(dir.toString()).build();
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of(""));
        when(fileRepository.readBytes(modelFile)).thenReturn("model".getBytes());

        byte[] result = service.loadModelSetFile("id", 0, "model.glb");

        assertThat(result).isEqualTo("model".getBytes());
    }

    @Test
    void testHasTransferableModel() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        CreationModelSet modelSet = CreationModelSet.builder().directory("dir").build();

        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("root"));

        // Case 1: Model exists
        when(fileRepository.list(Path.of("root/dir"))).thenReturn(List.of(Path.of("model.glb")));
        assertThat(service.hasTransferableModel("id", 0)).isTrue();

        // Case 2: No model exists
        when(fileRepository.list(Path.of("root/dir"))).thenReturn(List.of(Path.of("image.png")));
        assertThat(service.hasTransferableModel("id", 0)).isFalse();
    }

    @Test
    void testTransferModelToMediaCopiesAndSaves() {
        Path sourceDir = Path.of("src-dir");
        Path targetDir = Path.of("target-dir");

        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        CreationModelSet modelSet = CreationModelSet.builder().directory(sourceDir.toString()).build();
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of(""));
        when(useProjectDirsUseCase.getModelsDir("id")).thenReturn(targetDir);
        when(fileRepository.getNextAssetNumber(targetDir)).thenReturn(1);
        when(fileRepository.getExtension("m.glb")).thenReturn(Optional.of("glb"));
        when(fileRepository.getAssetName(1, "glb")).thenReturn("1.glb");
        when(item.getMediaContent().getModels()).thenReturn(new LinkedList<>());
        when(fileRepository.list(sourceDir)).thenReturn(List.of(sourceDir.resolve("m.glb")));

        service.transferModelToMedia("id", 0);

        verify(fileRepository).copy(eq(sourceDir.resolve("m.glb")), eq(targetDir.resolve("1.glb")), any());
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testTransferModelToMediaThrowsWhenFileRepositoryCopyFails() {
        Path sourceDir = Path.of("nonexistent-source-dir");
        Path targetDir = Path.of("nonexistent-target-dir");

        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        CreationModelSet modelSet = CreationModelSet.builder().directory(sourceDir.toString()).build();

        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of(""));
        when(useProjectDirsUseCase.getModelsDir("id")).thenReturn(targetDir);

        when(fileRepository.list(sourceDir)).thenReturn(List.of(sourceDir.resolve("m.glb")));
        when(fileRepository.getNextAssetNumber(targetDir)).thenReturn(1);
        when(fileRepository.getExtension("m.glb")).thenReturn(Optional.of("glb"));
        when(fileRepository.getAssetName(1, "glb")).thenReturn("1.glb");
        when(item.getMediaContent().getModels()).thenReturn(new LinkedList<>());
        doThrow(new ArtivactException("Could not copy resource!"))
                .when(fileRepository).copy(any(Path.class), any(Path.class), any());

        assertThatThrownBy(() -> service.transferModelToMedia("id", 0))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not copy resource!");
    }

    @Test
    void testDeleteModelSetRemovesAndSaves() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        CreationModelSet modelSet = CreationModelSet.builder().directory("dir").build();
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getModelSets()).thenReturn(new LinkedList<>(List.of(modelSet)));
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("root"));

        service.deleteModelSet("id", 0);

        verify(fileRepository).delete(any());
        verify(saveItemUseCase).save(item);
    }

    @Test
    void testToggleModelInputTogglesAndSaves() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getImageSets()).thenReturn(new LinkedList<>());
        item.getMediaCreationContent().getImageSets().add(
                com.arassec.artivact.domain.model.item.CreationImageSet.builder().modelInput(false).build()
        );

        service.toggleModelInput("id", 0);

        assertThat(item.getMediaCreationContent().getImageSets().getFirst().isModelInput()).isTrue();
        verify(saveItemUseCase).save(item);
    }

}
