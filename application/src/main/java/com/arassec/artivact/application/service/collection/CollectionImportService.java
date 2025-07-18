package com.arassec.artivact.application.service.collection;

import com.arassec.artivact.application.port.in.collection.ImportCollectionUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.CollectionExportRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionImportService implements ImportCollectionUseCase {

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final UseProjectDirsUseCase getProjectRootUseCase;

    private final FileRepository fileRepository;

    private final ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    private final ImportMenuUseCase importMenuUseCase;

    private final CollectionExportRepository collectionExportRepository;

    private final ObjectMapper objectMapper;

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
            Path existingCollectionExportFile = getProjectRootUseCase.getProjectRoot()
                    .resolve(DirectoryDefinitions.EXPORT_DIR)
                    .resolve(file.getFileName().toString());
            if (fileRepository.exists(existingCollectionExportFile)) {
                fileRepository.delete(existingCollectionExportFile);
            }

            Path tempDir = getProjectRootUseCase.getProjectRoot().resolve(DirectoryDefinitions.TEMP_DIR);

            ImportContext importContext = ImportContext.builder()
                    .importDir(tempDir.resolve(file.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                    .build();

            fileRepository.unpack(file, importContext.getImportDir());

            ExchangeMainData exchangeMainData = readExchangeMainDataJson(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON));

            if (!ContentSource.MENU.equals(exchangeMainData.getContentSource())) {
                throw new ArtivactException("Unsupported content source: " + exchangeMainData.getContentSource());
            }

            if (!onlyForDistribution) {
                importPropertiesConfigurationUseCase.importPropertiesConfiguration(importContext);
                importTagsConfigurationUseCase.importTagsConfiguration(importContext);
                importMenuUseCase.importMenu(importContext, exchangeMainData.getSourceId(), true);
            }

            fileRepository.copy(file, getProjectRootUseCase.getProjectRoot()
                            .resolve(DirectoryDefinitions.EXPORT_DIR)
                            .resolve(exchangeMainData.getId() + COLLECTION_EXCHANGE_SUFFIX + ZIP_FILE_SUFFIX),
                    StandardCopyOption.REPLACE_EXISTING);

            if (exchangeMainData.getCoverPictureExtension() != null
                    && !exchangeMainData.getCoverPictureExtension().trim().isEmpty()) {
                Path coverPictureFile = importContext.getImportDir().resolve("cover-picture." + exchangeMainData.getCoverPictureExtension());
                if (fileRepository.exists(coverPictureFile)) {
                    fileRepository.copy(coverPictureFile, getProjectRootUseCase.getProjectRoot()
                            .resolve(DirectoryDefinitions.EXPORT_DIR)
                            .resolve(exchangeMainData.getId() + "." + exchangeMainData.getCoverPictureExtension()));
                }
            }

            fileRepository.delete(importContext.getImportDir());

            collectionExportRepository.save(createCollectionExport(exchangeMainData, onlyForDistribution));
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
        result.setExportConfiguration(exchangeMainData.getExportConfiguration());
        result.setContentSource(ContentSource.MENU);
        result.setSourceId(exchangeMainData.getSourceId());
        result.setCoverPictureExtension(exchangeMainData.getCoverPictureExtension());
        result.setDistributionOnly(distributionOnly);
        return result;
    }

    private ExchangeMainData readExchangeMainDataJson(Path file) {
        try {
            return objectMapper.readValue(file.toFile(), ExchangeMainData.class);
        } catch (IOException e) {
            throw new ArtivactException("Could not read JSON file " + file, e);
        }
    }

}
