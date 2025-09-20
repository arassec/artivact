package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageItemImagesServiceTest {

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;
    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;
    @Mock
    private LoadItemUseCase loadItemUseCase;
    @Mock
    private SaveItemUseCase saveItemUseCase;
    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private ManageItemImagesService service;

    @Test
    void testLoadImageReturnsBytes() {
        Path path = Path.of("test.png");
        FileSystemResource fsr = mock(FileSystemResource.class);
        when(fileRepository.loadImage(any(), any(), any(), any(), any())).thenReturn(fsr);
        when(fsr.getFile()).thenReturn(path.toFile());
        when(fileRepository.readBytes(path)).thenReturn(new byte[]{1, 2, 3});

        byte[] result = service.loadImage("id", "file", ImageSize.ORIGINAL);

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void testSaveImageDelegatesToFileRepository() {
        InputStream stream = mock(InputStream.class);

        service.saveImage("id", "file", stream, true);

        verify(fileRepository).saveFile(any(), eq("id"), eq("file"), eq(stream), any(), isNull(), eq(true));
    }

    @Test
    void testAddImageHappyPath() throws Exception {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        MultipartFile file = mock(MultipartFile.class);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(file.getOriginalFilename()).thenReturn("f.png");
        when(file.getInputStream()).thenReturn(mock(InputStream.class));
        when(fileRepository.saveFile(any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn("f.png");
        when(item.getMediaContent().getImages()).thenReturn(new java.util.LinkedList<>());

        service.addImage("id", file);

        verify(saveItemUseCase).save(item);
    }

    @Test
    void testAddImageThrowsIfItemNotFound() {
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(null);
        MultipartFile file = mock(MultipartFile.class);

        assertThatThrownBy(() -> service.addImage("id", file))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No item found");
    }

    @Test
    void testAddImageThrowsOnIOException() throws Exception {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        MultipartFile file = mock(MultipartFile.class);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(file.getOriginalFilename()).thenReturn("f.png");
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        assertThatThrownBy(() -> service.addImage("id", file))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not add image!");
    }

    @Test
    void testCreateImageSetFromDanglingImagesAddsSet() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        when(loadItemUseCase.loadTranslated("id")).thenReturn(item);
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(Path.of("items"));
        when(fileRepository.getDirFromId(any(Path.class), eq("id"))).thenReturn(Path.of("item-image-dir"));

        List<String> danglingImages = new LinkedList<>();
        danglingImages.add("dangling.png");
        when(fileRepository.listNamesWithoutScaledImages(any())).thenReturn(danglingImages);

        when(item.getId()).thenReturn("id");
        when(item.getMediaContent().getImages()).thenReturn(new java.util.LinkedList<>());
        when(item.getMediaCreationContent().getImageSets()).thenReturn(new java.util.LinkedList<>());

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service.createImageSetFromDanglingImages("id");

        ArgumentCaptor<Item> argCap = ArgumentCaptor.forClass(Item.class);
        verify(saveItemUseCase).save(argCap.capture());

        assertThat(argCap.getValue().getMediaCreationContent().getImageSets()).hasSize(1);
        assertThat(argCap.getValue().getMediaCreationContent().getImageSets()
                .getFirst().getFiles().getFirst()).isEqualTo("dangling.png");
    }

    @Test
    void testOpenImagesDirDelegatesToFileRepository() {
        Path path = Path.of("images");
        when(useProjectDirsUseCase.getImagesDir("id")).thenReturn(path);

        service.openImagesDir("id");

        verify(fileRepository).openDirInOs(path);
    }

    @Test
    void testTransferImageToMediaCopiesAndSaves() throws Exception {
        Asset asset = new Asset();
        asset.setFileName("img.png");
        Path source = Files.createTempFile("source", ".png");
        Path imagesDir = source.getParent();
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);

        when(useProjectDirsUseCase.getImagesDir("id")).thenReturn(imagesDir);
        when(fileRepository.getNextAssetNumber(imagesDir)).thenReturn(1);
        when(fileRepository.getExtension("img.png")).thenReturn(Optional.of("png"));
        when(fileRepository.getAssetName(1, "png")).thenReturn("1.png");
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaContent().getImages()).thenReturn(new java.util.LinkedList<>());

        service.transferImageToMedia("id", asset);

        verify(saveItemUseCase).save(item);
    }

    @Test
    void testDeleteImageSetRemovesAndSaves() {
        Item item = mock(Item.class, RETURNS_DEEP_STUBS);
        when(loadItemUseCase.loadTranslatedRestricted("id")).thenReturn(item);
        when(item.getMediaCreationContent().getImageSets()).thenReturn(new java.util.LinkedList<>(List.of(CreationImageSet.builder().build())));

        service.deleteImageSet("id", 0);

        verify(saveItemUseCase).save(item);
    }

}
