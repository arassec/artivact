package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ImportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.TranslatableString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

/**
 * Service for collection import.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionImportService implements ImportCollectionUseCase {

    /**
     * Use case for run background operation.
     */
    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    /**
     * Use case for get project root.
     */
    private final UseProjectDirsUseCase getProjectRootUseCase;

    /**
     * Repository for file.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for import tags configuration.
     */
    private final ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    /**
     * Use case for import properties configuration.
     */
    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    /**
     * Use case for import menu.
     */
    private final ImportMenuUseCase importMenuUseCase;

    /**
     * Repository for collection export.
     */
    private final CollectionExportRepository collectionExportRepository;

    /**
     * The JSON mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * Imports previously created collection exports to the application by reading from the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    @Override
    public synchronized void importCollection(Path file) {
        importCollection(file, false);
    }

    /**
     * Imports previously created collection exports to the application by saving the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    @Override
    public synchronized void importCollectionForDistribution(Path file) {
        importCollection(file, true);
    }

    /**
     * Imports a previously created collection export.
     *
     * @param file                The export file.
     * @param onlyForDistribution Set to {@code true}, if only the file should be imported, {@code false} if also the
     *                            content (menus, pages, items) should be imported.
     */
    private synchronized void importCollection(Path file, boolean onlyForDistribution) {
        runBackgroundOperationUseCase.execute("collectionImport", "import", progressMonitor -> {
            Path existingCollectionExportFile = getProjectRootUseCase.getExportsDir()
                    .resolve(file.getFileName().toString());
            if (fileRepository.exists(existingCollectionExportFile)) {
                fileRepository.delete(existingCollectionExportFile);
            }

            Path tempDir = getProjectRootUseCase.getTempDir();

            ImportContext importContext = ImportContext.builder()
                    .importDir(tempDir.resolve(file.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                    .build();

            fileRepository.unpack(file, importContext.getImportDir());

            ExchangeMainData exchangeMainData =
                    jsonMapper.readValue(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile(), ExchangeMainData.class);

            if (!ContentSource.COLLECTION.equals(exchangeMainData.getContentSource())) {
                throw new ArtivactException("Unsupported content source: " + exchangeMainData.getContentSource());
            }

            if (!onlyForDistribution) {
                importPropertiesConfigurationUseCase.importPropertiesConfiguration(importContext);
                importTagsConfigurationUseCase.importTagsConfiguration(importContext);
                exchangeMainData.getSourceIds().forEach(menuId -> importMenuUseCase.importMenu(importContext, menuId, true));
            }

            fileRepository.copy(file, getProjectRootUseCase.getExportsDir()
                            .resolve(exchangeMainData.getId() + COLLECTION_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX),
                    StandardCopyOption.REPLACE_EXISTING);

            if (exchangeMainData.getCoverPictureExtension() != null
                    && !exchangeMainData.getCoverPictureExtension().trim().isEmpty()) {
                Path coverPictureFile = importContext.getImportDir().resolve("cover-picture." + exchangeMainData.getCoverPictureExtension());
                if (fileRepository.exists(coverPictureFile)) {
                    fileRepository.copy(coverPictureFile, getProjectRootUseCase.getExportsDir()
                                    .resolve(exchangeMainData.getId() + "." + exchangeMainData.getCoverPictureExtension()),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }

            importContentAudioFiles(importContext, exchangeMainData);

            fileRepository.delete(importContext.getImportDir());
            fileRepository.delete(file);

            collectionExportRepository.save(createCollectionExport(exchangeMainData, onlyForDistribution));

            progressMonitor.updateProgress(1, 1);
        });
    }

    /**
     * Creates the collection export configuration.
     *
     * @param exchangeMainData Main data of the export.
     * @param distributionOnly Set to {@code true}, if this collection export has only been imported for further distribution.
     * @return A newly created {@link CollectionExport} instance.
     */
    private static CollectionExport createCollectionExport(ExchangeMainData exchangeMainData, boolean distributionOnly) {
        CollectionExport result = new CollectionExport();
        result.setId(exchangeMainData.getId());
        result.setTitle(exchangeMainData.getTitle());
        result.setDescription(exchangeMainData.getDescription());
        result.setContent(exchangeMainData.getContent());
        result.setExportConfiguration(exchangeMainData.getExportConfiguration());
        result.setContentSource(ContentSource.MENU);
        result.setSourceId(exchangeMainData.getSourceIds().getFirst());
        result.setCoverPictureExtension(exchangeMainData.getCoverPictureExtension());
        result.setContentAudio(exchangeMainData.getContentAudio());
        result.setDistributionOnly(distributionOnly);
        return result;
    }

    /**
     * Imports content audio files from the import directory to the exports directory.
     *
     * @param importContext    The import context.
     * @param exchangeMainData The exchange main data.
     */
    private void importContentAudioFiles(ImportContext importContext, ExchangeMainData exchangeMainData) {
        TranslatableString contentAudio = exchangeMainData.getContentAudio();
        if (contentAudio == null) {
            return;
        }
        if (StringUtils.hasText(contentAudio.getValue())) {
            copyAudioFileFromImport(importContext, contentAudio.getValue());
        }
        if (contentAudio.getTranslations() != null) {
            contentAudio.getTranslations().values().forEach(filename -> {
                if (StringUtils.hasText(filename)) {
                    copyAudioFileFromImport(importContext, filename);
                }
            });
        }
    }

    /**
     * Copies a single audio file from the import directory to the exports directory.
     *
     * @param importContext The import context.
     * @param filename     The audio filename.
     */
    private void copyAudioFileFromImport(ImportContext importContext, String filename) {
        Path audioFile = importContext.getImportDir().resolve(filename);
        if (fileRepository.exists(audioFile)) {
            fileRepository.copy(audioFile, getProjectRootUseCase.getExportsDir().resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
