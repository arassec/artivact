package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.exporter.ItemExporter;
import com.arassec.artivact.domain.exchange.exporter.MenuExporter;
import com.arassec.artivact.domain.exchange.exporter.PropertiesExporter;
import com.arassec.artivact.domain.exchange.exporter.TagsExporter;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.*;

/**
 * Standard {@link ArtivactExporter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactStandardExporter implements ArtivactExporter {

    /**
     * Exporter for the property configuration.
     */
    private final PropertiesExporter propertiesExporter;

    /**
     * Exporter for the tag configuration.
     */
    private final TagsExporter tagsExporter;

    /**
     * Exporter for items.
     */
    private final ItemExporter itemExporter;

    /**
     * Exporter for menus.
     */
    private final MenuExporter menuExporter;

    /**
     * The application's file repository.
     */
    private final FileRepository fileRepository;

    /**
     * The application's project data provider.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportCollection(CollectionExport collectionExport, Menu menu, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration) {
        ExportContext exportContext = createExportContext(collectionExport.getId(), collectionExport.getExportConfiguration());
        exportContext.setId(collectionExport.getId());
        exportContext.setCoverPictureExtension(collectionExport.getCoverPictureExtension());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), collectionExport.getTitle(), collectionExport.getDescription());

        propertiesExporter.exportPropertiesConfiguration(exportContext, propertiesConfiguration);
        tagsExporter.exportTagsConfiguration(exportContext, tagsConfiguration);

        menuExporter.exportMenu(exportContext, menu);

        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPictureFile = projectDataProvider.getProjectRoot()
                    .resolve(ProjectDataProvider.EXPORT_DIR)
                    .resolve(collectionExport.getId() + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPictureFile)) {
                fileRepository.copy(coverPictureFile, exportContext.getExportDir()
                        .resolve("cover-picture." + collectionExport.getCoverPictureExtension()));
            }
        }

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportMenu(Menu menu) {
        ExportContext exportContext = createExportContext(menu.getId(), ExportConfiguration.builder()
                .applyRestrictions(false)
                .optimizeSize(false)
                .excludeItems(true)
                .build());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), null, null);

        menuExporter.exportMenu(exportContext, menu);

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(Item item, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration) {
        ExportContext exportContext = createExportContext(item.getId(), null);

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.ITEM, item.getId(), item.getTitle(), item.getDescription());

        propertiesExporter.exportPropertiesConfiguration(exportContext, propertiesConfiguration);
        tagsExporter.exportTagsConfiguration(exportContext, tagsConfiguration);

        itemExporter.exportItem(exportContext, item);

        fileRepository.pack(exportContext.getExportDir(), exportContext.getExportFile());
        fileRepository.delete(exportContext.getExportDir());

        return exportContext.getExportFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportPropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        return propertiesExporter.exportPropertiesConfiguration(ExportContext.builder()
                .exportDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build(), propertiesConfiguration
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportTagsConfiguration(TagsConfiguration tagsConfiguration) {
        return tagsExporter.exportTagsConfiguration(ExportContext.builder()
                .exportDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build(), tagsConfiguration
        );
    }

    /**
     * Prepares the export by cleaning up and creating necessary directories and creating the {@link ExportContext}.
     *
     * @param exportContext The export context.
     */
    private void prepareExport(ExportContext exportContext) {
        if (fileRepository.exists(exportContext.getExportFile())) {
            fileRepository.delete(exportContext.getExportFile());
        }

        if (fileRepository.exists(exportContext.getExportDir())) {
            fileRepository.delete(exportContext.getExportDir());
        }

        fileRepository.createDirIfRequired(exportContext.getExportDir());
    }

    /**
     * Creates the export context.
     *
     * @param id                  The ID of the export.
     * @param exportConfiguration An optional export configuration.
     * @return A newly created {@link ExportContext}.
     */
    private ExportContext createExportContext(String id, ExportConfiguration exportConfiguration) {
        String exportName = id + COLLECTION_EXCHANGE_SUFFIX;
        ExportContext exportContext = new ExportContext();
        exportContext.setExportConfiguration(Optional.ofNullable(exportConfiguration).orElse(new ExportConfiguration()));
        exportContext.setProjectExportsDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR));
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
    private void exportMainData(ExportContext exportContext, ContentSource contentSource, String exportSourceId, TranslatableString title, TranslatableString description) {
        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setContentSource(contentSource);
        exchangeMainData.setSourceId(exportSourceId);
        exchangeMainData.setTitle(Optional.ofNullable(title).orElse(new TranslatableString()));
        exchangeMainData.getTitle().setTranslatedValue(null);
        exchangeMainData.setDescription(Optional.ofNullable(description).orElse(new TranslatableString()));
        exchangeMainData.getDescription().setTranslatedValue(null);
        exchangeMainData.setId(exportContext.getId());
        exchangeMainData.setExportConfiguration(exportContext.getExportConfiguration());
        exchangeMainData.setCoverPictureExtension(exportContext.getCoverPictureExtension());
        writeJsonFile(exportContext.getExportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON), exchangeMainData);
    }

    /**
     * Writes the supplied object as JSON file.
     *
     * @param targetPath The target path including the file name.
     * @param object     The object to write.
     */
    private void writeJsonFile(Path targetPath, Object object) {
        try {
            objectMapper.writeValue(targetPath.toFile(), object);
        } catch (IOException e) {
            throw new ArtivactException("Could not write export file!", e);
        }
    }

}
