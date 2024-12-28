package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.imp.ItemImporter;
import com.arassec.artivact.domain.exchange.imp.MenuImporter;
import com.arassec.artivact.domain.exchange.imp.PropertiesImporter;
import com.arassec.artivact.domain.exchange.imp.TagsImporter;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON;
import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.ZIP_FILE_SUFFIX;

/**
 * Standard {@link ArtivactImporter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactStandardImporter implements ArtivactImporter {

    /**
     * Importer for items.
     */
    private final ItemImporter itemImporter;

    /**
     * Importer for menus.
     */
    private final MenuImporter menuImporter;

    /**
     * Importer for the property configuration.
     */
    private final PropertiesImporter propertiesImporter;

    /**
     * Importer for the tags configuration.
     */
    private final TagsImporter tagsImporter;

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void importContent(Path contentExport) {
        ImportContext importContext = ImportContext.builder()
                .importDir(projectDataProvider.getProjectRoot()
                        .resolve(ProjectDataProvider.TEMP_DIR)
                        .resolve(contentExport.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                .build();

        fileRepository.unpack(contentExport, importContext.getImportDir());

        try {
            ExchangeMainData exchangeMainData = objectMapper.readValue(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile(), ExchangeMainData.class);

            if (ContentSource.MENU.equals(exchangeMainData.getContentSource())) {
                menuImporter.importMenu(importContext, exchangeMainData.getSourceId(), true);
            } else if (ContentSource.ITEM.equals(exchangeMainData.getContentSource())) {
                itemImporter.importItem(importContext, exchangeMainData.getSourceId());
            } else {
                throw new ArtivactException("Unknown content source: " + exchangeMainData.getContentSource());
            }

            propertiesImporter.importPropertiesConfiguration(importContext);
            tagsImporter.importTagsConfiguration(importContext);

            fileRepository.delete(importContext.getImportDir());

        } catch (Exception e) {
            throw new ArtivactException("Could not import data!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionExport importCollection(Path collectionExportFile, boolean distributionOnly) {
        ImportContext importContext = ImportContext.builder()
                .importDir(projectDataProvider.getProjectRoot()
                        .resolve(ProjectDataProvider.TEMP_DIR)
                        .resolve(collectionExportFile.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                .build();

        fileRepository.unpack(collectionExportFile, importContext.getImportDir());

        try {
            ExchangeMainData exchangeMainData = objectMapper.readValue(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile(), ExchangeMainData.class);

            if (!ContentSource.MENU.equals(exchangeMainData.getContentSource())) {
                throw new ArtivactException("Unsupported content source: " + exchangeMainData.getContentSource());
            }

            if (!distributionOnly) {
                propertiesImporter.importPropertiesConfiguration(importContext);
                tagsImporter.importTagsConfiguration(importContext);
                menuImporter.importMenu(importContext, exchangeMainData.getSourceId(), true);
            }

            fileRepository.copy(collectionExportFile, projectDataProvider.getProjectRoot()
                    .resolve(ProjectDataProvider.EXPORT_DIR)
                    .resolve(exchangeMainData.getId() + ".zip"));

            if (StringUtils.hasText(exchangeMainData.getCoverPictureExtension())) {
                Path coverPictureFile = importContext.getImportDir().resolve("cover-picture." + exchangeMainData.getCoverPictureExtension());
                if (fileRepository.exists(coverPictureFile)) {
                    fileRepository.copy(coverPictureFile, projectDataProvider.getProjectRoot()
                            .resolve(ProjectDataProvider.EXPORT_DIR)
                            .resolve(exchangeMainData.getId() + "." + exchangeMainData.getCoverPictureExtension()));
                }
            }

            fileRepository.delete(importContext.getImportDir());

            return createCollectionExport(exchangeMainData, distributionOnly);
        } catch (Exception e) {
            throw new ArtivactException("Could not import data!", e);
        }
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

}
