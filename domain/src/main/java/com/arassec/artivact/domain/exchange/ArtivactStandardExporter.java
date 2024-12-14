package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.PageService;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Standard {@link ArtivactExporter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactStandardExporter implements ArtivactExporter, ExchangeProcessor {

    /**
     * The application's configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The service for page handling.
     */
    private final PageService pageService;

    /**
     * The service for searching items.
     */
    private final SearchService searchService;

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
    public Path exportCollection(CollectionExport collectionExport, Menu menu) {
        ExportContext exportContext = createExportContext(collectionExport.getId(), collectionExport.getExportConfiguration());
        exportContext.setId(collectionExport.getId());
        exportContext.setCoverPictureExtension(collectionExport.getCoverPictureExtension());

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), menu.getExportTitle(), menu.getExportDescription());

        exportPropertiesConfiguration(exportContext);
        exportTagsConfiguration(exportContext);

        exportMenu(exportContext, menu);

        if (StringUtils.hasText(collectionExport.getCoverPictureExtension())) {
            Path coverPictureFile = projectDataProvider.getProjectRoot()
                    .resolve(ProjectDataProvider.EXPORT_DIR)
                    .resolve(collectionExport.getId() + "." + collectionExport.getCoverPictureExtension());
            if (fileRepository.exists(coverPictureFile)) {
                fileRepository.copy(coverPictureFile, exportContext.getExportDir()
                        .resolve("cover-picture." + collectionExport.getCoverPictureExtension()));
            }
        }

        fileRepository.pack(exportContext.getExportDir().toAbsolutePath(), exportContext.getExportFile().toAbsolutePath());
        fileRepository.delete(exportContext.getExportDir().toAbsolutePath());

        return exportContext.getExportFile().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportMenu(ExportConfiguration exportConfiguration, Menu menu) {
        ExportContext exportContext = createExportContext(menu.getId(), exportConfiguration);

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.MENU, menu.getId(), menu.getExportTitle(), menu.getExportDescription());

        // Properties and tags configuration is only exported if items are exported as well.
        if (!exportConfiguration.isExcludeItems()) {
            exportPropertiesConfiguration(exportContext);
            exportTagsConfiguration(exportContext);
        }

        exportMenu(exportContext, menu);

        fileRepository.pack(exportContext.getExportDir().toAbsolutePath(), exportContext.getExportFile().toAbsolutePath());
        fileRepository.delete(exportContext.getExportDir().toAbsolutePath());

        return exportContext.getExportFile().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(Item item) {
        ExportContext exportContext = createExportContext(item.getId(), null);

        prepareExport(exportContext);

        exportMainData(exportContext, ContentSource.ITEM, item.getId(), item.getTitle(), item.getDescription());
        exportPropertiesConfiguration(exportContext);
        exportTagsConfiguration(exportContext);

        exportItem(exportContext, item);

        fileRepository.pack(exportContext.getExportDir().toAbsolutePath(), exportContext.getExportFile().toAbsolutePath());

        return exportContext.getExportFile().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportPropertiesConfiguration() {
        return exportPropertiesConfiguration(ExportContext.builder()
                .exportDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportTagsConfiguration() {
        return exportTagsConfiguration(ExportContext.builder()
                .exportDir(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXPORT_DIR))
                .exportConfiguration(new ExportConfiguration())
                .build()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExchangeMainData readExchangeMainData(Path path) {
        try {
            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equals(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON)) {
                        InputStream stream = zipFile.getInputStream(entry);
                        return objectMapper.readValue(stream.readAllBytes(), ExchangeMainData.class);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Couldn't read export main data from ZIP file.", e);
        }
        throw new ArtivactException("Could not read export main data file!");
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
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    private Path exportPropertiesConfiguration(ExportContext exportContext) {
        Path exportFile = exportContext.getExportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        PropertiesConfiguration propertiesConfiguration = configurationService.loadPropertiesConfiguration();
        cleanupPropertyConfiguration(exportContext, propertiesConfiguration);
        writeJsonFile(exportFile, propertiesConfiguration);
        return exportFile;
    }

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    private Path exportTagsConfiguration(ExportContext exportContext) {
        Path exportFile = exportContext.getExportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration();
        cleanupTagsConfiguration(exportContext, tagsConfiguration);
        writeJsonFile(exportFile, tagsConfiguration);
        return exportFile;
    }

    /**
     * Exports a menu.
     *
     * @param exportContext Context of the export.
     * @param menu          The menu to export.
     */
    private void exportMenu(ExportContext exportContext, Menu menu) {
        cleanupMenu(exportContext, menu);

        writeJsonFile(exportContext.getExportDir().resolve(menu.getId() + MENU_EXCHANGE_FILE_SUFFIX), menu);

        Optional.ofNullable(menu.getMenuEntries()).orElse(List.of())
                .forEach(menuEntry -> exportMenu(exportContext, menuEntry));

        if (StringUtils.hasText(menu.getTargetPageId())) {
            var pageContent = pageService.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER));
            exportPage(exportContext, menu.getTargetPageId(), pageContent);
        }
    }

    /**
     * Exports a page.
     *
     * @param exportContext Context of the content export.
     * @param pageContent   The page to export.
     */
    private void exportPage(ExportContext exportContext, String targetPageId, PageContent pageContent) {
        if (exportContext.getExportConfiguration().isApplyRestrictions() && !pageContent.getRestrictions().isEmpty()) {
            return;
        }

        pageContent.getWidgets().forEach(widget -> {
            if (exportContext.getExportConfiguration().isApplyRestrictions() && !widget.getRestrictions().isEmpty()) {
                return;
            }
            exportWidget(exportContext, widget);
        });

        writeJsonFile(exportContext.getExportDir().resolve(targetPageId + PAGE_EXCHANGE_FILE_SUFFIX), pageContent);
    }

    /**
     * Exports the supplied widget.
     *
     * @param exportContext Context of the content export.
     * @param widget        The widget to export.
     */
    private void exportWidget(ExportContext exportContext, Widget widget) {
        widget.getNavigationTitle().setTranslatedValue(null);
        switch (widget) {
            case AvatarWidget avatarWidget -> {
                avatarWidget.getAvatarSubtext().setTranslatedValue(null);
                copyWidgetFile(exportContext, avatarWidget, avatarWidget.getAvatarImage());
            }
            case ImageTextWidget imageTextWidget -> {
                imageTextWidget.getText().setTranslatedValue(null);
                copyWidgetFile(exportContext, imageTextWidget, imageTextWidget.getImage());
            }
            case InfoBoxWidget infoBoxWidget -> {
                infoBoxWidget.getHeading().setTranslatedValue(null);
                infoBoxWidget.getContent().setTranslatedValue(null);
            }
            case PageTitleWidget pageTitleWidget -> {
                pageTitleWidget.getTitle().setTranslatedValue(null);
                copyWidgetFile(exportContext, pageTitleWidget, pageTitleWidget.getBackgroundImage());
            }
            case ItemSearchWidget itemSearchWidget -> {
                itemSearchWidget.getHeading().setTranslatedValue(null);
                itemSearchWidget.getContent().setTranslatedValue(null);
                exportItemSearchWidgetsItems(exportContext, itemSearchWidget);
            }
            case TextWidget textWidget -> {
                textWidget.getHeading().setTranslatedValue(null);
                textWidget.getContent().setTranslatedValue(null);
            }
            default -> log.info("No export available for widget type: {}", widget.getType());
        }
    }

    /**
     * Exports an {@link ItemSearchWidget}'s item list if required.
     *
     * @param exportContext    The export context.
     * @param itemSearchWidget The widget to export the item list for.
     */
    private void exportItemSearchWidgetsItems(ExportContext exportContext, ItemSearchWidget itemSearchWidget) {
        if (!exportContext.getExportConfiguration().isExcludeItems()) {
            String searchTerm = itemSearchWidget.getSearchTerm();
            int maxResults = itemSearchWidget.getMaxResults();
            List<Item> searchResult = searchService.search(searchTerm, maxResults);
            if (searchResult != null && !searchResult.isEmpty()) {
                for (Item item : searchResult) {
                    if (exportContext.getExportConfiguration().isApplyRestrictions() && !item.getRestrictions().isEmpty()) {
                        continue;
                    }
                    exportItem(exportContext, item);
                }
                writeJsonFile(exportContext.getExportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX),
                        searchResult.stream().map(Item::getId).toArray());
            }
        }
    }

    /**
     * Exports an item with its media files.
     *
     * @param exportContext The export context.
     * @param item          The item to export.
     */
    private void exportItem(ExportContext exportContext, Item item) {
        Path itemExportDir = exportContext.getExportDir().resolve(item.getId());

        // Already exported by another widget / exporter? Skip item...
        if (fileRepository.exists(itemExportDir)) {
            return;
        } else {
            fileRepository.createDirIfRequired(itemExportDir);
        }

        copyItemMediaFiles(exportContext, item, itemExportDir);

        item.getTitle().setTranslatedValue(null);
        item.getDescription().setTranslatedValue(null);

        writeJsonFile(itemExportDir.resolve(ITEM_EXCHANGE_FILENAME_JSON), item);
    }

    /**
     * Copies the item's media files to the target directory.
     *
     * @param exportContext Export context.
     * @param item          The item to export.
     * @param itemExportDir The item's export directory.
     */
    private void copyItemMediaFiles(ExportContext exportContext, Item item, Path itemExportDir) {
        List<String> images = item.getMediaContent().getImages();
        List<String> models = item.getMediaContent().getModels();

        item.setMediaCreationContent(null); // Not needed in standard exports at the moment.

        Path imagesSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.IMAGES_DIR);
        Path modelsSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.MODELS_DIR);

        if (exportContext.getExportConfiguration().isOptimizeSize()) {
            if (!models.isEmpty()) {
                String firstModel = models.getFirst();
                fileRepository.copy(modelsSourceDir.resolve(firstModel), itemExportDir.resolve(firstModel));
                item.getMediaContent().getImages().clear();
                item.getMediaContent().getModels().retainAll(List.of(firstModel));
            } else if (!images.isEmpty()) {
                String firstImage = images.getFirst();
                fileRepository.copy(imagesSourceDir.resolve(firstImage), itemExportDir.resolve(firstImage));
                item.getMediaContent().getModels().clear();
                item.getMediaContent().getImages().retainAll(List.of(firstImage));
            }
        } else {
            item.getMediaContent().getModels()
                    .forEach(model -> fileRepository.copy(modelsSourceDir.resolve(model), itemExportDir.resolve(model)));
            item.getMediaContent().getImages()
                    .forEach(image -> fileRepository.copy(imagesSourceDir.resolve(image), itemExportDir.resolve(image)));
        }
    }

    /**
     * Copies a widget file to the widget's export directory.
     *
     * @param exportContext Export context.
     * @param widget        The widget to copy files from.
     * @param file          The file to copy.
     */
    private void copyWidgetFile(ExportContext exportContext, Widget widget, String file) {
        if (StringUtils.hasText(file)) {
            Path sourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_DIR), widget.getId());
            Path targetDir = exportContext.getExportDir().resolve(widget.getId());
            fileRepository.createDirIfRequired(targetDir);
            fileRepository.copy(sourceDir.resolve(file), targetDir.resolve(file));
        }
    }

    /**
     * Cleans the menu up for export.
     *
     * @param exportContext Export context.
     * @param menu          The menu to clean up.
     */
    private void cleanupMenu(ExportContext exportContext, Menu menu) {
        cleanupTranslations(menu);
        if (menu.getMenuEntries() != null) {
            menu.getMenuEntries().stream()
                    .filter(menuEntry -> {
                        if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                            return menuEntry.getRestrictions().isEmpty();
                        }
                        return true;
                    })
                    .forEach(menuEntry -> {
                        cleanupTranslations(menuEntry);
                        menuEntry.setMenuEntries(menuEntry.getMenuEntries().isEmpty() ? null : menuEntry.getMenuEntries());
                    });
        }
    }

    /**
     * Cleans up property categories for export.
     *
     * @param exportContext           Export context.
     * @param propertiesConfiguration The property configuration to clean up.
     */
    private void cleanupPropertyConfiguration(ExportContext exportContext, PropertiesConfiguration propertiesConfiguration) {
        propertiesConfiguration.getCategories().stream()
                .filter(propertyCategory -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(propertyCategory -> {
                    cleanupTranslations(propertyCategory);
                    propertyCategory.getProperties().stream()
                            .filter(property -> {
                                if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                                    return property.getRestrictions().isEmpty();
                                }
                                return true;
                            })
                            .forEach(property -> {
                                cleanupTranslations(property);
                                if (property.getValueRange() != null && !property.getValueRange().isEmpty()) {
                                    property.getValueRange().stream()
                                            .filter(propertyValue -> {
                                                if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                                                    return propertyValue.getRestrictions().isEmpty();
                                                }
                                                return true;
                                            })
                                            .forEach(this::cleanupTranslations);
                                }
                            });
                });
    }

    /**
     * Cleans up tags configuration for export.
     *
     * @param exportContext     Export context.
     * @param tagsConfiguration The tags configuration to clean up.
     */
    private void cleanupTagsConfiguration(ExportContext exportContext, TagsConfiguration tagsConfiguration) {
        tagsConfiguration.getTags().stream()
                .filter(propertyCategory -> {
                    if (exportContext.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(this::cleanupTranslations);
    }

    /**
     * Removes restrictions and a translated value from the supplied object.
     *
     * @param translatableRestrictedObject The object to clean up.
     */
    private void cleanupTranslations(BaseTranslatableRestrictedObject translatableRestrictedObject) {
        translatableRestrictedObject.setTranslatedValue(null);
    }

    /**
     * Writes the supplied object as JSON file.
     *
     * @param targetPath The target path including the file name.
     * @param object     The object to write.
     */
    private void writeJsonFile(Path targetPath, Object object) {
        try {
            objectMapper.writeValue(targetPath.toAbsolutePath().toFile(), object);
        } catch (IOException e) {
            throw new ArtivactException("Could not write export file!", e);
        }
    }
}
