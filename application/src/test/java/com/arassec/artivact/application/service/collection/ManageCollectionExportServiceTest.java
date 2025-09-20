package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ExportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class ManageCollectionExportServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private CollectionExportRepository collectionExportRepository;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @Mock
    private LoadMenuUseCase loadMenuUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExportCollectionUseCase exportCollectionUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @InjectMocks
    private ManageCollectionExportService service;

    private CollectionExport export;

    @BeforeEach
    void setUp() {
        export = new CollectionExport();
        export.setId("test-id");
        export.setSourceId("source-1");
        export.setTitle(new TranslatableString("title"));
    }

    @Test
    void testLoad() {
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(fileRepository.exists(any(Path.class))).thenReturn(true);
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));
        when(fileRepository.lastModified(any(Path.class))).thenReturn(Instant.now());
        when(fileRepository.size(any(Path.class))).thenReturn(12345L);

        CollectionExport collectionExport = service.load("test-id");

        assertThat(collectionExport).isNotNull();
        assertThat(collectionExport.getId()).isEqualTo("test-id");
        assertThat(collectionExport.getFileLastModified()).isGreaterThan(0L);
        assertThat(collectionExport.getFileSize()).isEqualTo(12345L);
    }

    @Test
    void testLoadAllRestrictedDelegatesToLoadAll() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));
        when(collectionExportRepository.findAll()).thenReturn(List.of(export));
        var result = service.loadAllRestricted();
        assertThat(result).hasSize(1);
        verify(collectionExportRepository).findAll();
    }

    @Test
    void testSaveThrowsExceptionOnInvalidExport() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(ArtivactException.class);
    }

    @Test
    void testSaveStoresValidExport() {
        service.save(export);
        verify(collectionExportRepository).save(export);
    }

    @Test
    void testSaveSortOrderDelegates() {
        service.saveSortOrder(List.of(export));
        verify(collectionExportRepository).saveSortOrder(anyList());
    }

    @Test
    void testDeleteRemovesFileCoverAndRepositoryEntry() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        export.setCoverPictureExtension("jpg");

        service.delete("test-id");

        verify(fileRepository, atLeastOnce()).delete(any());
        verify(collectionExportRepository).delete("test-id");
    }

    @SuppressWarnings("resource")
    @Test
    void testReadExportFileThrowsWhenFileMissing() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(false);

        assertThatThrownBy(() -> service.readExportFile("id"))
                .isInstanceOf(ArtivactException.class);
    }

    @Test
    void testBuildExportFileRunsBackgroundOperation() {
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service.buildExportFile("test-id");

        verify(collectionExportRepository).save(export);
    }

    @Test
    void testSaveCoverPictureStoresFileAndUpdatesExport() {
        when(fileRepository.getExtension("pic.jpg")).thenReturn(Optional.of("jpg"));
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));

        service.saveCoverPicture("test-id", "pic.jpg", new ByteArrayInputStream(new byte[0]));

        assertThat(export.getCoverPictureExtension()).isEqualTo("jpg");
        verify(collectionExportRepository).save(export);
    }

    @Test
    void testLoadCoverPictureThrowsIfMissing() {
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        assertThatThrownBy(() -> service.loadCoverPicture("test-id"))
                .isInstanceOf(ArtivactException.class);
    }

    @Test
    void testDeleteCoverPictureRemovesFileAndResetsExtension() {
        export.setCoverPictureExtension("jpg");
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);

        service.deleteCoverPicture("test-id");

        assertThat(export.getCoverPictureExtension()).isNull();
        verify(collectionExportRepository).save(export);
    }

    @Test
    void testCreateCollectionExportInfosWritesToStream() throws Exception {
        export.setFilePresent(true);
        export.setFileSize(42L);
        export.setFileLastModified(Instant.now().toEpochMilli());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.list(any())).thenReturn(List.of());
        when(objectMapper.writeValueAsBytes(any())).thenReturn("data".getBytes());

        service.createCollectionExportInfos(outputStream, List.of(export));

        assertThat(outputStream.toByteArray()).isNotEmpty();
    }

}
