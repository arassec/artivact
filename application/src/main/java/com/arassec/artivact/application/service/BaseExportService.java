package com.arassec.artivact.application.service;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableObject;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.Optional;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

/**
 * Base class for exporters.
 */
public abstract class BaseExportService {

    /**
     * The object mapper.
     *
     * @return The {@link JsonMapper} for object mapping.
     */
    public abstract JsonMapper getJsonMapper();

    /**
     * Returns the file repository.
     */
    public abstract FileRepository getFileRepository();

    /**
     * Returns the use project dirs use case.
     */
    public abstract UseProjectDirsUseCase getUseProjectDirsUseCase();

    /**
     * Removes restrictions and a translated value from the supplied object.
     *
     * @param translatableRestrictedObject The object to clean up.
     */
    protected void cleanupTranslations(TranslatableObject translatableRestrictedObject) {
        Optional.ofNullable(translatableRestrictedObject).ifPresent(TranslatableObject::clear);
    }

    /**
     * Writes the supplied object as JSON file.
     *
     * @param targetPath The target path including the file name.
     * @param object     The object to write.
     */
    protected void writeJsonFile(Path targetPath, Object object) {
        getJsonMapper().writeValue(targetPath.toFile(), object);
    }


    /**
     * Prepares the export by cleaning up and creating necessary directories and creating the {@link ExportContext}.
     *
     * @param exportContext The export context.
     */
    protected void prepareExport(ExportContext exportContext) {
        if (getFileRepository().exists(exportContext.getExportFile())) {
            getFileRepository().delete(exportContext.getExportFile());
        }

        if (getFileRepository().exists(exportContext.getExportDir())) {
            getFileRepository().delete(exportContext.getExportDir());
        }

        getFileRepository().createDirIfRequired(exportContext.getExportDir());
    }

    /**
     * Creates the export context.
     *
     * @param id                  The ID of the export.
     * @param exportConfiguration An optional export configuration.
     * @return A newly created {@link ExportContext}.
     */
    protected ExportContext createExportContext(String id, ExportConfiguration exportConfiguration) {
        String exportName = id + COLLECTION_EXCHANGE_SUFFIX;
        ExportContext exportContext = new ExportContext();
        exportContext.setExportConfiguration(Optional.ofNullable(exportConfiguration).orElse(new ExportConfiguration()));
        exportContext.setProjectExportsDir(getUseProjectDirsUseCase().getExportsDir());
        exportContext.setExportDir(exportContext.getProjectExportsDir().resolve(exportName));
        exportContext.setExportFile(exportContext.getProjectExportsDir().resolve(exportName + ZIP_FILE_SUFFIX));
        return exportContext;
    }

    /**
     * Exports the main data of the export like title and description.
     *
     * @param exportContext  The export context.
     * @param contentSource  The {@link ContentSource} of the export.
     * @param exportSourceId The ID of the export's source object.
     * @param title          The export's title.
     * @param description    The export's description.
     */
    protected void exportMainData(ExportContext exportContext,
                                  ContentSource contentSource,
                                  String exportSourceId,
                                  TranslatableString title,
                                  TranslatableString description,
                                  TranslatableString content) {
        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(contentSource);
        exchangeMainData.setSourceId(exportSourceId);
        exchangeMainData.setTitle(Optional.ofNullable(title).orElse(new TranslatableString()));
        exchangeMainData.getTitle().setTranslatedValue(null);
        exchangeMainData.setDescription(Optional.ofNullable(description).orElse(new TranslatableString()));
        exchangeMainData.getDescription().setTranslatedValue(null);
        exchangeMainData.setContent(Optional.ofNullable(content).orElse(new TranslatableString()));
        exchangeMainData.getContent().setTranslatedValue(null);
        exchangeMainData.setId(exportContext.getId());
        exchangeMainData.setExportConfiguration(exportContext.getExportConfiguration());
        exchangeMainData.setCoverPictureExtension(exportContext.getCoverPictureExtension());
        writeJsonFile(exportContext.getExportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON), exchangeMainData);
    }

}
