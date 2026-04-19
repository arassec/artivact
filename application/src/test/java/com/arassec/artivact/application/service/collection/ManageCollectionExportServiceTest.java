package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ExportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
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
import com.arassec.artivact.domain.model.exchange.CollectionExportInfo;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageCollectionExportServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private CollectionExportRepository collectionExportRepository;

    @Mock
    private LoadMenuUseCase loadMenuUseCase;

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

    @TempDir
    Path tempDir;

    private ManageCollectionExportService service;

    private Path exportsDir;

    @BeforeEach
    void setUp() {
        exportsDir = Path.of("/tmp/exports");
        lenient().when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);

        service = new ManageCollectionExportService(
                fileRepository,
                collectionExportRepository,
                loadMenuUseCase,
                JsonMapper.builder().build(),
                exportCollectionUseCase,
                useProjectDirsUseCase,
                runBackgroundOperationUseCase,
                aiGateway,
                loadAiConfigurationUseCase
        );
    }

    @Test
    void saveStoresCollectionExportWithRequiredParameters() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");

        service.save(collectionExport);

        verify(collectionExportRepository).save(collectionExport);
    }

    @ParameterizedTest
    @MethodSource("invalidCollectionExports")
    void saveRejectsCollectionExportsWithoutRequiredParameters(CollectionExport collectionExport) {
        assertThatThrownBy(() -> service.save(collectionExport))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Cannot save a collection export without required parameters");
    }

    @Test
    void loadAllAddsExportFileMetadataContentAndResolvedAudio() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(null);

        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        Path defaultAudioFile = exportsDir.resolve("export-1.mp3");
        Path localizedAudioFile = exportsDir.resolve("export-1-de.mp3");

        when(collectionExportRepository.findAll()).thenReturn(List.of(collectionExport));
        when(fileRepository.exists(exportFile)).thenReturn(true);
        when(fileRepository.lastModified(exportFile)).thenReturn(Instant.ofEpochMilli(1234L));
        when(fileRepository.size(exportFile)).thenReturn(2048L);
        when(fileRepository.exists(defaultAudioFile)).thenReturn(true);
        when(fileRepository.list(exportsDir)).thenReturn(List.of(localizedAudioFile, exportsDir.resolve("other.mp3")));

        List<CollectionExport> result = service.loadAll();

        CollectionExport loadedCollectionExport = result.getFirst();
        assertThat(loadedCollectionExport.isFilePresent()).isTrue();
        assertThat(loadedCollectionExport.getFileLastModified()).isEqualTo(1234L);
        assertThat(loadedCollectionExport.getFileSize()).isEqualTo(2048L);
        assertThat(loadedCollectionExport.getContent()).isNotNull();
        assertThat(loadedCollectionExport.getContent().getValue()).isEmpty();
        assertThat(loadedCollectionExport.getContentAudio().getValue()).isEqualTo("export-1.mp3");
        assertThat(loadedCollectionExport.getContentAudio().getTranslations())
                .containsEntry("de", "export-1-de.mp3");
    }

    @Test
    void loadAllRestrictedReturnsCollectionExportsWithDefaultMetadataWhenNoFilesExist() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(null);

        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        Path defaultAudioFile = exportsDir.resolve("export-1.mp3");

        when(collectionExportRepository.findAll()).thenReturn(List.of(collectionExport));
        when(fileRepository.exists(exportFile)).thenReturn(false);
        when(fileRepository.exists(defaultAudioFile)).thenReturn(false);
        when(fileRepository.list(exportsDir)).thenReturn(List.of(exportsDir.resolve("other.mp3")));

        List<CollectionExport> result = service.loadAllRestricted();

        CollectionExport loadedCollectionExport = result.getFirst();
        assertThat(loadedCollectionExport.isFilePresent()).isFalse();
        assertThat(loadedCollectionExport.getContent()).isNotNull();
        assertThat(loadedCollectionExport.getContent().getValue()).isEmpty();
        assertThat(loadedCollectionExport.getContentAudio().getValue()).isEmpty();
        assertThat(loadedCollectionExport.getContentAudio().getTranslations()).isEmpty();
    }

    @Test
    void loadReturnsCollectionExportWithResolvedFileAndAudioInformation() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(null);

        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        Path defaultAudioFile = exportsDir.resolve("export-1.mp3");
        Path localizedAudioFile = exportsDir.resolve("export-1-de.mp3");

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(fileRepository.exists(exportFile)).thenReturn(true);
        when(fileRepository.lastModified(exportFile)).thenReturn(Instant.ofEpochMilli(1234L));
        when(fileRepository.size(exportFile)).thenReturn(2048L);
        when(fileRepository.exists(defaultAudioFile)).thenReturn(true);
        when(fileRepository.list(exportsDir)).thenReturn(List.of(localizedAudioFile));

        CollectionExport loadedCollectionExport = service.load("export-1");

        assertThat(loadedCollectionExport.isFilePresent()).isTrue();
        assertThat(loadedCollectionExport.getFileLastModified()).isEqualTo(1234L);
        assertThat(loadedCollectionExport.getFileSize()).isEqualTo(2048L);
        assertThat(loadedCollectionExport.getContent().getValue()).isEmpty();
        assertThat(loadedCollectionExport.getContentAudio().getValue()).isEqualTo("export-1.mp3");
        assertThat(loadedCollectionExport.getContentAudio().getTranslations())
                .containsEntry("de", "export-1-de.mp3");
    }

    @Test
    void readExportFileReturnsConfiguredInputStream() {
        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        when(fileRepository.exists(exportFile)).thenReturn(true);
        when(fileRepository.readStream(exportFile)).thenReturn(inputStream);

        InputStream result = service.readExportFile("export-1");

        assertThat(result).isSameAs(inputStream);
    }

    @SuppressWarnings("resource")
    @Test
    void readExportFileThrowsExceptionWhenExportDoesNotExist() {
        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        when(fileRepository.exists(exportFile)).thenReturn(false);

        assertThatThrownBy(() -> service.readExportFile("export-1"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Cannot read export file for export with ID: export-1");
    }

    @Test
    void deleteRemovesExportArtifactsAndRepositoryEntry() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setCoverPictureExtension("jpg");

        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");
        Path coverPicture = exportsDir.resolve("export-1.jpg");
        Path defaultAudioFile = exportsDir.resolve("export-1.mp3");
        Path localizedAudioFile = exportsDir.resolve("export-1-de.mp3");

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(fileRepository.exists(exportFile)).thenReturn(true);
        when(fileRepository.exists(coverPicture)).thenReturn(true);
        when(fileRepository.exists(defaultAudioFile)).thenReturn(true);
        when(fileRepository.list(exportsDir)).thenReturn(List.of(localizedAudioFile, exportsDir.resolve("other.mp3")));

        service.delete("export-1");

        verify(fileRepository).delete(exportFile);
        verify(fileRepository).delete(coverPicture);
        verify(fileRepository).delete(defaultAudioFile);
        verify(fileRepository).delete(localizedAudioFile);
        verify(collectionExportRepository).save(collectionExport);
        verify(collectionExportRepository).delete("export-1");
        assertThat(collectionExport.getCoverPictureExtension()).isNull();
    }

    @Test
    void saveCoverPictureScalesImageAndPersistsExtension() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        when(fileRepository.getExtension("cover.jpg")).thenReturn(Optional.of("jpg"));
        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));

        service.saveCoverPicture("export-1", "cover.jpg", inputStream);

        verify(fileRepository).createDirIfRequired(exportsDir);
        verify(fileRepository).scaleImage(
                inputStream,
                exportsDir.resolve("export-1.jpg"),
                "jpg",
                ImageSize.PAGE_TITLE.getWidth()
        );
        verify(collectionExportRepository).save(collectionExport);
        assertThat(collectionExport.getCoverPictureExtension()).isEqualTo("jpg");
    }

    @Test
    void loadCoverPictureThrowsExceptionWhenNoCoverPictureIsConfigured() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setCoverPictureExtension(" ");

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));

        assertThatThrownBy(() -> service.loadCoverPicture("export-1"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No cover picture available for collection export: export-1");
    }

    @Test
    void loadCoverPictureReturnsStoredBytes() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setCoverPictureExtension("jpg");
        byte[] coverPictureBytes = new byte[]{4, 5, 6};

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(fileRepository.readBytes(exportsDir.resolve("export-1.jpg"))).thenReturn(coverPictureBytes);

        byte[] result = service.loadCoverPicture("export-1");

        assertThat(result).containsExactly(4, 5, 6);
    }

    @Test
    void deleteCoverPictureClearsConfiguredExtensionWhenFileIsMissing() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setCoverPictureExtension("jpg");

        Path coverPicture = exportsDir.resolve("export-1.jpg");

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(fileRepository.exists(coverPicture)).thenReturn(false);

        service.deleteCoverPicture("export-1");

        assertThat(collectionExport.getCoverPictureExtension()).isNull();
        verify(fileRepository, never()).delete(coverPicture);
        verify(collectionExportRepository).save(collectionExport);
    }

    @Test
    void createCollectionExportInfosWritesOverviewAndMediaFilesToZip() throws IOException {
        Path coverPicture = Files.writeString(tempDir.resolve("cover.jpg"), "cover-content");
        Path ignoredZip = Files.writeString(tempDir.resolve("ignored.zip"), "zip-content");
        Path directory = Files.createDirectory(tempDir.resolve("subdir"));

        CollectionExport availableExport = createCollectionExport("export-1", "menu-1", "Title");
        availableExport.setDescription(new TranslatableString("Description"));
        availableExport.setFilePresent(true);
        availableExport.setFileSize(512L);
        availableExport.setFileLastModified(42L);

        CollectionExport unavailableExport = createCollectionExport("export-2", "menu-2", "Other");
        unavailableExport.setFilePresent(false);

        when(useProjectDirsUseCase.getExportsDir()).thenReturn(tempDir);
        when(fileRepository.list(tempDir)).thenReturn(List.of(coverPicture, ignoredZip, directory));
        when(fileRepository.isDir(coverPicture)).thenReturn(false);
        when(fileRepository.isDir(directory)).thenReturn(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        service.createCollectionExportInfos(outputStream, List.of(availableExport, unavailableExport));

        Map<String, byte[]> zipEntries = readZipEntries(outputStream.toByteArray());
        CollectionExportInfo[] collectionExportInfos = JsonMapper.builder().build().readValue(
                zipEntries.get(ExchangeDefinitions.COLLECTION_EXPORT_OVERVIEWS_JSON_FILE),
                CollectionExportInfo[].class
        );

        assertThat(zipEntries).containsKeys(ExchangeDefinitions.COLLECTION_EXPORT_OVERVIEWS_JSON_FILE, "cover.jpg")
                .doesNotContainKey("ignored.zip");
        assertThat(collectionExportInfos).hasSize(1);
        assertThat(collectionExportInfos[0].getId()).isEqualTo("export-1");
        assertThat(collectionExportInfos[0].getTitle().getValue()).isEqualTo("Title");
        assertThat(zipEntries.get("cover.jpg")).containsExactly("cover-content".getBytes());
    }

    @Test
    void createCollectionExportInfosThrowsExceptionWhenTargetStreamCannotBeWritten() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setFilePresent(true);

        try (OutputStream failingOutputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("boom");
            }
        }) {
            List<CollectionExport> collectionExports = List.of(collectionExport);
            assertThatThrownBy(() -> service.createCollectionExportInfos(failingOutputStream, collectionExports))
                    .isInstanceOf(ArtivactException.class)
                    .hasMessageContaining("Could not create item export file!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveSortOrderPassesCollectionExportsToRepository() {
        List<CollectionExport> collectionExports = List.of(
                createCollectionExport("export-1", "menu-1", "Title"),
                createCollectionExport("export-2", "menu-2", "Other")
        );

        service.saveSortOrder(collectionExports);

        verify(collectionExportRepository).saveSortOrder(collectionExports);
    }

    @Test
    void saveContentAudioCopiesAudioToLocalizedFilename() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{7, 8, 9});

        service.saveContentAudio("export-1", "de", "audio.mp3", inputStream);

        verify(fileRepository).createDirIfRequired(exportsDir);
        verify(fileRepository).copy(
                inputStream,
                exportsDir.resolve("export-1-de.mp3"),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    @ParameterizedTest
    @MethodSource("defaultLocales")
    void saveContentAudioCopiesAudioToDefaultFilenameWhenLocaleIsBlank(String locale) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{7, 8, 9});

        service.saveContentAudio("export-1", locale, "audio.mp3", inputStream);

        verify(fileRepository).createDirIfRequired(exportsDir);
        verify(fileRepository).copy(
                inputStream,
                exportsDir.resolve("export-1.mp3"),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    @Test
    void saveContentAudioRejectsInvalidLocale() {
        var contentAudio = new ByteArrayInputStream(new byte[]{7, 8, 9});
        assertThatThrownBy(() -> service.saveContentAudio(
                "export-1",
                "../etc",
                "audio.mp3",
                contentAudio
        ))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");

        verifyNoInteractions(fileRepository);
    }

    @Test
    void loadContentAudioReturnsBytesForExistingAudioFile() {
        Path audioFile = exportsDir.resolve("export-1-de.mp3");
        when(fileRepository.exists(audioFile)).thenReturn(true);
        when(fileRepository.readBytes(audioFile)).thenReturn(new byte[]{1, 3, 5});

        byte[] result = service.loadContentAudio("export-1", "export-1-de.mp3");

        assertThat(result).containsExactly(1, 3, 5);
    }

    @Test
    void loadContentAudioRejectsInvalidFileNames() {
        assertThatThrownBy(() -> service.loadContentAudio("export-1", "../export-1.mp3"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid content audio filename");
    }

    @Test
    void loadContentAudioThrowsExceptionWhenAudioFileDoesNotExist() {
        Path audioFile = exportsDir.resolve("export-1-de.mp3");
        when(fileRepository.exists(audioFile)).thenReturn(false);

        assertThatThrownBy(() -> service.loadContentAudio("export-1", "export-1-de.mp3"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Content audio file not found: export-1-de.mp3");
    }

    @Test
    void deleteContentAudioRemovesExistingAudioFile() {
        Path audioFile = exportsDir.resolve("export-1-de.mp3");
        when(fileRepository.exists(audioFile)).thenReturn(true);

        service.deleteContentAudio("export-1", "de");

        verify(fileRepository).delete(audioFile);
    }

    @Test
    void deleteContentAudioDoesNothingWhenAudioFileDoesNotExist() {
        Path audioFile = exportsDir.resolve("export-1.mp3");
        when(fileRepository.exists(audioFile)).thenReturn(false);

        service.deleteContentAudio("export-1", null);

        verify(fileRepository, never()).delete(any(Path.class));
    }

    @Test
    void deleteContentAudioRejectsInvalidLocale() {
        assertThatThrownBy(() -> service.deleteContentAudio("export-1", "../etc"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");

        verifyNoInteractions(fileRepository);
    }

    @Test
    void generateContentAudioRejectsInvalidLocale() {
        assertThatThrownBy(() -> service.generateContentAudio("export-1", "../etc"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");

        verifyNoInteractions(collectionExportRepository, loadAiConfigurationUseCase, aiGateway);
    }

    @Test
    void generateContentAudioThrowsExceptionWhenContentIsMissing() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(null);

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));

        assertThatThrownBy(() -> service.generateContentAudio("export-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No content available for audio generation in collection export: export-1");

        verifyNoInteractions(loadAiConfigurationUseCase, aiGateway);
    }

    @Test
    void generateContentAudioThrowsExceptionWhenContentIsBlank() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(new TranslatableString(""));

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));

        assertThatThrownBy(() -> service.generateContentAudio("export-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No content available for audio generation in collection export: export-1");
    }

    @Test
    void generateContentAudioUsesTranslatedContentAndVoice() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        TranslatableString content = new TranslatableString("Hello");
        content.getTranslations().put("de", "Hallo");
        collectionExport.setContent(content);

        AiConfiguration aiConfiguration = new AiConfiguration();
        TranslatableString ttsVoice = new TranslatableString("voice-default");
        ttsVoice.getTranslations().put("de", "voice-de");
        aiConfiguration.setTtsVoice(ttsVoice);

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        String audioFilename = service.generateContentAudio("export-1", "de");

        assertThat(audioFilename).isEqualTo("export-1-de.mp3");
        verify(fileRepository).createDirIfRequired(exportsDir);
        verify(aiGateway).convertToAudio(
                aiConfiguration,
                "Hallo",
                "voice-de",
                exportsDir.resolve("export-1-de.mp3")
        );
    }

    @Test
    void generateContentAudioUsesDefaultContentAndVoiceWhenLocaleIsBlank() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(new TranslatableString("Hello"));

        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsVoice(new TranslatableString("voice-default"));

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        String audioFilename = service.generateContentAudio("export-1", "");

        assertThat(audioFilename).isEqualTo("export-1.mp3");
        verify(aiGateway).convertToAudio(
                aiConfiguration,
                "Hello",
                "voice-default",
                exportsDir.resolve("export-1.mp3")
        );
    }

    @Test
    void generateContentAudioFallsBackToOriginalContentAndVoiceWhenTranslationIsMissing() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(new TranslatableString("Hello"));

        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsVoice(new TranslatableString("voice-default"));

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        String audioFilename = service.generateContentAudio("export-1", "fr");

        assertThat(audioFilename).isEqualTo("export-1-fr.mp3");
        verify(aiGateway).convertToAudio(
                aiConfiguration,
                "Hello",
                "voice-default",
                exportsDir.resolve("export-1-fr.mp3")
        );
    }

    @Test
    void buildExportFileRunsBackgroundExportAndPersistsTheUpdatedCollectionExport() {
        CollectionExport collectionExport = createCollectionExport("export-1", "menu-1", "Title");
        collectionExport.setContent(null);
        Menu menu = new Menu();
        menu.setId("menu-1");
        Path exportFile = exportsDir.resolve("export-1.artivact.collection.zip");

        when(collectionExportRepository.findById("export-1")).thenReturn(Optional.of(collectionExport));
        when(fileRepository.exists(exportFile)).thenReturn(false);
        when(fileRepository.list(exportsDir)).thenReturn(List.of());
        when(loadMenuUseCase.loadMenu("menu-1")).thenReturn(menu);
        when(exportCollectionUseCase.exportCollection(collectionExport, menu)).thenReturn(exportFile);

        doAnswer(invocation -> {
            invocation.<com.arassec.artivact.domain.model.operation.BackgroundOperation>getArgument(2)
                    .execute(new ProgressMonitor("collectionExport", "export"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(eq("collectionExport"), eq("export"), any());

        service.buildExportFile("export-1");

        verify(loadMenuUseCase).loadMenu("menu-1");
        verify(exportCollectionUseCase).exportCollection(collectionExport, menu);
        verify(collectionExportRepository).save(collectionExport);
    }

    private static Stream<Arguments> invalidCollectionExports() {
        CollectionExport missingId = createCollectionExport("export-1", "menu-1", "Title");
        missingId.setId(null);

        CollectionExport missingSourceId = createCollectionExport("export-1", "menu-1", "Title");
        missingSourceId.setSourceId(" ");

        CollectionExport missingTitle = createCollectionExport("export-1", "menu-1", "Title");
        missingTitle.setTitle(new TranslatableString(" "));

        return Stream.of(
                Arguments.of((CollectionExport) null),
                Arguments.of(missingId),
                Arguments.of(missingSourceId),
                Arguments.of(missingTitle)
        );
    }

    private static Stream<Arguments> defaultLocales() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of(" ")
        );
    }

    private static CollectionExport createCollectionExport(String id, String sourceId, String title) {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setId(id);
        collectionExport.setSourceId(sourceId);
        collectionExport.setTitle(new TranslatableString(title));
        collectionExport.setDescription(new TranslatableString("Description"));
        return collectionExport;
    }

    private Map<String, byte[]> readZipEntries(byte[] zipContent) throws IOException {
        Map<String, byte[]> zipEntries = new HashMap<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                zipEntries.put(zipEntry.getName(), zipInputStream.readAllBytes());
                zipInputStream.closeEntry();
            }
        }

        return zipEntries;
    }

}
