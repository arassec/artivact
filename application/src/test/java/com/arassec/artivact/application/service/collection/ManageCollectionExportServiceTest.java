package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ExportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    private JsonMapper jsonMapper;

    @Mock
    private ExportCollectionUseCase exportCollectionUseCase;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private AiGateway aiGateway;

    @Mock
    private LoadAiConfigurationUseCase loadAiConfigurationUseCase;

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
        when(fileRepository.list(Path.of("exports"))).thenReturn(List.of());

        CollectionExport collectionExport = service.load("test-id");

        assertThat(collectionExport).isNotNull();
        assertThat(collectionExport.getId()).isEqualTo("test-id");
        assertThat(collectionExport.getFileLastModified()).isGreaterThan(0L);
        assertThat(collectionExport.getFileSize()).isEqualTo(12345L);
    }

    @Test
    void testLoadResolvesContentAudioFromFilesystem() {
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));
        when(fileRepository.exists(Path.of("exports/test-id.artivact.collection.zip"))).thenReturn(false);
        when(fileRepository.exists(Path.of("exports/test-id.mp3"))).thenReturn(true);
        when(fileRepository.list(Path.of("exports"))).thenReturn(List.of(
                Path.of("exports/test-id-de.mp3"),
                Path.of("exports/test-id-fr.mp3"),
                Path.of("exports/other-file.txt")
        ));

        CollectionExport collectionExport = service.load("test-id");

        assertThat(collectionExport.getContentAudio()).isNotNull();
        assertThat(collectionExport.getContentAudio().getValue()).isEqualTo("test-id.mp3");
        assertThat(collectionExport.getContentAudio().getTranslations()).containsEntry("de", "test-id-de.mp3");
        assertThat(collectionExport.getContentAudio().getTranslations()).containsEntry("fr", "test-id-fr.mp3");
        assertThat(collectionExport.getContentAudio().getTranslations()).hasSize(2);
    }

    @Test
    void testLoadAllRestrictedDelegatesToLoadAll() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));
        when(collectionExportRepository.findAll()).thenReturn(List.of(export));
        when(fileRepository.list(Path.of("exports"))).thenReturn(List.of());
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
        when(fileRepository.list(Path.of("/tmp"))).thenReturn(List.of());
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
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("exports"));
        when(fileRepository.list(Path.of("exports"))).thenReturn(List.of());

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
    void testCreateCollectionExportInfosWritesToStream() {
        export.setFilePresent(true);
        export.setFileSize(42L);
        export.setFileLastModified(Instant.now().toEpochMilli());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.list(any())).thenReturn(List.of());
        when(jsonMapper.writeValueAsBytes(any())).thenReturn("data".getBytes());

        service.createCollectionExportInfos(outputStream, List.of(export));

        assertThat(outputStream.toByteArray()).isNotEmpty();
    }

    @Test
    void testSaveContentAudioStoresFileAndUpdatesExport(@TempDir Path tempDir) {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(tempDir);

        doAnswer(invocation -> {
            Path target = invocation.getArgument(1);
            java.nio.file.Files.createFile(target);
            return null;
        }).when(fileRepository).copy(any(InputStream.class), any(Path.class), any());

        service.saveContentAudio("test-id", "de", "audio.mp3", new ByteArrayInputStream(new byte[0]));

        assertThat(tempDir.resolve("test-id-de.mp3")).exists();
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testSaveContentAudioDefaultLocale(@TempDir Path tempDir) {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(tempDir);

        doAnswer(invocation -> {
            Path target = invocation.getArgument(1);
            java.nio.file.Files.createFile(target);
            return null;
        }).when(fileRepository).copy(any(InputStream.class), any(Path.class), any());

        service.saveContentAudio("test-id", "", "audio.mp3", new ByteArrayInputStream(new byte[0]));

        assertThat(tempDir.resolve("test-id.mp3")).exists();
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testDeleteContentAudioRemovesFile() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);

        service.deleteContentAudio("test-id", "de");

        verify(fileRepository).delete(Path.of("/tmp/test-id-de.mp3"));
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testDeleteContentAudioDefaultLocale() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);

        service.deleteContentAudio("test-id", "");

        verify(fileRepository).delete(Path.of("/tmp/test-id.mp3"));
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testDeleteContentAudioFileNotPresent() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(false);

        service.deleteContentAudio("test-id", "de");

        verify(fileRepository, never()).delete(any());
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testLoadContentAudioNotFound() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(false);

        assertThatThrownBy(() -> service.loadContentAudio("test-id", "test-id.mp3"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Content audio file not found");
    }

    @Test
    void testLoadContentAudioInvalidFilename() {
        assertThatThrownBy(() -> service.loadContentAudio("test-id", "../etc/passwd"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid content audio filename");
    }

    @Test
    void testLoadContentAudioSuccess() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);
        when(fileRepository.readBytes(any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = service.loadContentAudio("test-id", "test-id.mp3");

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void testGenerateContentAudioInvalidLocale() {
        assertThatThrownBy(() -> service.generateContentAudio("test-id", "../etc"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");
    }

    @Test
    void testGenerateContentAudioNoContent() {
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));

        assertThatThrownBy(() -> service.generateContentAudio("test-id", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No content available");
    }

    @Test
    void testGenerateContentAudioSuccess() {
        TranslatableString content = new TranslatableString("Some content text");
        export.setContent(content);

        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(new AiConfiguration());

        String result = service.generateContentAudio("test-id", "");

        assertThat(result).isEqualTo("test-id.mp3");
        verify(aiGateway).convertToAudio(any(AiConfiguration.class), eq("Some content text"), eq("alloy"), any(Path.class));
        verify(collectionExportRepository, never()).save(any());
    }

    @Test
    void testDeleteRemovesContentAudioFiles() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(any())).thenReturn(true);
        when(collectionExportRepository.findById("test-id")).thenReturn(Optional.of(export));
        when(fileRepository.list(Path.of("/tmp"))).thenReturn(List.of(
                Path.of("/tmp/test-id-de.mp3"),
                Path.of("/tmp/test-id-fr.mp3")
        ));

        export.setCoverPictureExtension("jpg");

        service.delete("test-id");

        // Deletes: export file, cover picture, default audio, 2 localized audio files = at least 4 delete calls
        verify(fileRepository, atLeast(4)).delete(any());
        verify(collectionExportRepository).delete("test-id");
    }

}
