package com.arassec.artivact.adapter.in.rest.controller.collection;

import com.arassec.artivact.application.port.in.collection.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionExportControllerTest {

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ImportCollectionUseCase importCollectionUseCase;

    @Mock
    private LoadCollectionExportUseCase loadCollectionExportUseCase;

    @Mock
    private SaveCollectionExportUseCase saveCollectionExportUseCase;

    @Mock
    private DeleteCollectionExportUseCase deleteCollectionExportUseCase;

    @Mock
    private BuildCollectionExportFileUseCase buildCollectionExportFileUseCase;

    @Mock
    private ReadCollectionExportFileUseCase readCollectionExportFileUseCase;

    @Mock
    private SaveCollectionExportCoverPictureUseCase saveCollectionExportCoverPictureUseCase;

    @Mock
    private LoadCollectionExportCoverPictureUseCase loadCollectionExportCoverPictureUseCase;

    @Mock
    private DeleteCollectionExportCoverPictureUseCase deleteCollectionExportCoverPictureUseCase;

    @Mock
    private CreateCollectionExportInfosUseCase createCollectionExportInfosUseCase;

    @InjectMocks
    private CollectionExportController controller;

    @Test
    void testLoadCollectionExports() {
        List<CollectionExport> exports = List.of(new CollectionExport());
        when(loadCollectionExportUseCase.loadAll()).thenReturn(exports);

        List<CollectionExport> result = controller.loadCollectionExports();

        assertThat(result).isEqualTo(exports);
        verify(loadCollectionExportUseCase).loadAll();
    }

    @Test
    void testSave() {
        CollectionExport export = new CollectionExport();
        List<CollectionExport> exports = List.of(export);
        when(loadCollectionExportUseCase.loadAll()).thenReturn(exports);

        List<CollectionExport> result = controller.save(export);

        assertThat(result).isEqualTo(exports);
        verify(saveCollectionExportUseCase).save(export);
    }

    @Test
    void testSaveSortOrder() {
        List<CollectionExport> exports = List.of(new CollectionExport());
        when(loadCollectionExportUseCase.loadAll()).thenReturn(exports);

        List<CollectionExport> result = controller.saveSortOrder(exports);

        assertThat(result).isEqualTo(exports);
        verify(saveCollectionExportUseCase).saveSortOrder(exports);
    }

    @Test
    void testDelete() {
        List<CollectionExport> exports = List.of(new CollectionExport());
        when(loadCollectionExportUseCase.loadAll()).thenReturn(exports);

        List<CollectionExport> result = controller.delete("id-1");

        assertThat(result).isEqualTo(exports);
        verify(deleteCollectionExportUseCase).delete("id-1");
    }

    @Test
    void testBuild() {
        controller.build("id-1");
        verify(buildCollectionExportFileUseCase).buildExportFile("id-1");
    }

    @Test
    @SneakyThrows
    void testDownloadExportFile() {
        CollectionExport export = new CollectionExport();
        export.setFilePresent(true);
        export.setFileSize(3);
        when(loadCollectionExportUseCase.load("id-1")).thenReturn(export);
        when(readCollectionExportFileUseCase.readExportFile("id-1")).thenReturn(new ByteArrayInputStream("test".getBytes()));

        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseEntity<StreamingResponseBody> entity = controller.downloadExportFile("id-1", null, response);

        assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(entity.getBody()).isNotNull();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.getBody().writeTo(outputStream);

        assertThat(outputStream.toString()).isEqualTo("test");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testDownloadExportFileForbidden() {
        CollectionExport export = new CollectionExport();
        export.setFilePresent(true);
        export.setRestrictions(Set.of("ROLE_ADMIN"));
        when(loadCollectionExportUseCase.load("id-1")).thenReturn(export);

        GrantedAuthority grantedAuthority = mock(GrantedAuthority.class);
        when(grantedAuthority.getAuthority()).thenReturn("ROLE_USER");

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((Collection) List.of(grantedAuthority));

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> controller.downloadExportFile("id-1", auth, response))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Operation not allowed!");
    }

    @Test
    void testSaveCoverPicture() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", "data".getBytes());
        controller.saveCoverPicture("id-1", file);
        verify(saveCollectionExportCoverPictureUseCase).saveCoverPicture(eq("id-1"), eq("cover.png"), any());
    }

    @Test
    void testSaveCoverPictureIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("cover.png");
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        assertThatThrownBy(() -> controller.saveCoverPicture("id-1", file))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not save uploaded cover picture!");
    }

    @Test
    void testLoadCoverPicture() {
        CollectionExport export = new CollectionExport();
        export.setCoverPictureExtension("png");
        when(loadCollectionExportUseCase.load("id-1")).thenReturn(export);
        when(loadCollectionExportCoverPictureUseCase.loadCoverPicture("id-1")).thenReturn(new byte[]{1, 2, 3});

        HttpEntity<byte[]> result = controller.loadCoverPicture("id-1", "random");
        assertThat(result.getBody()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void testDeleteCoverPicture() {
        List<CollectionExport> exports = List.of(new CollectionExport());
        when(loadCollectionExportUseCase.loadAll()).thenReturn(exports);

        List<CollectionExport> result = controller.deleteCoverPicture("id-1");

        assertThat(result).isEqualTo(exports);
        verify(deleteCollectionExportCoverPictureUseCase).deleteCoverPicture("id-1");
    }

    @Test
    @SneakyThrows
    void testLoadCollectionExportInfos() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseEntity<StreamingResponseBody> entity = controller.loadCollectionExportInfos(response);

        assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(entity.getBody()).isNotNull();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.getBody().writeTo(outputStream);

        verify(createCollectionExportInfosUseCase).createCollectionExportInfos(any(OutputStream.class), anyList());
    }

    @Test
    void testImportCollection() {
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("tmp"));
        MultipartFile file = new MockMultipartFile("file", "export.zip", "application/zip", new byte[]{1, 2, 3});
        controller.importCollection(file);
        verify(importCollectionUseCase).importCollection(any());
    }

    @Test
    void testImportCollectionForDistribution() {
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("tmp"));
        MultipartFile file = new MockMultipartFile("file", "export.zip", "application/zip", new byte[]{1, 2, 3});
        controller.importCollectionForDistribution(file);
        verify(importCollectionUseCase).importCollectionForDistribution(any());
    }

}
