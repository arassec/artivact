package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.exchange.model.ExportMainData;
import com.arassec.artivact.domain.exchange.model.ExportSourceType;
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
     * File suffix for exported menus.
     */
    private static final String MENU_EXPORT_FILE_SUFFIX = ".artivact.menu.json";

    /**
     * File suffix for exported pages.
     */
    private static final String PAGE_EXPORT_FILE_SUFFIX = ".artivact.page-content.json";

    /**
     * File suffix for exported search results.
     */
    private static final String SEARCH_EXPORT_FILE_SUFFIX = ".artivact.search-result.json";

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
    public Path exportMenu(ExportConfiguration exportConfiguration, Menu menu) {
        ExportContext exportContext = createExportContext(menu.getId(), ExportSourceType.MENU, exportConfiguration);

        prepareExport(exportContext);

        exportMainData(exportContext, ExportSourceType.MENU, menu.getId(), menu.getExportTitle(), menu.getExportDescription());
        exportPropertiesConfiguration(exportContext);
        exportTagsConfiguration(exportContext);

        exportMenu(exportContext, menu);

        // Copy a cover picture if one exists:
        Optional<Path> coverPictureOptional = fileRepository.list(exportContext.getProjectExportsDir()).stream()
                .filter(file -> !fileRepository.isDir(file))
                .filter(file -> file.getFileName().toString().startsWith(menu.getId()))
                .findFirst();

        if (coverPictureOptional.isPresent()) {
            Path coverPicture = coverPictureOptional.get();
            fileRepository.copy(coverPicture, exportContext.getExportDir().resolve(coverPicture.getFileName()));
        }

        fileRepository.pack(exportContext.getExportDir().toAbsolutePath(), exportContext.getExportFile().toAbsolutePath());

        return exportContext.getExportFile().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path exportItem(Item item) {
        ExportContext exportContext = createExportContext(item.getId(), ExportSourceType.ITEM, null);

        prepareExport(exportContext);

        exportMainData(exportContext, ExportSourceType.ITEM, item.getId(), item.getTitle(), item.getDescription());
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
    public ExportMainData readExportMainData(Path path) {
        try {
            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equals(CONTENT_EXPORT_FILENAME_JSON)) {
                        InputStream stream = zipFile.getInputStream(entry);
                        return objectMapper.readValue(stream.readAllBytes(), ExportMainData.class);
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
     * @param exportSourceType    Type of the source the export is based on.
     * @param exportConfiguration An optional export configuration.
     * @return A newly created {@link ExportContext}.
     */
    private ExportContext createExportContext(String id, ExportSourceType exportSourceType, ExportConfiguration exportConfiguration) {
        String exportName = id + "." + exportSourceType.name() + CONTENT_EXPORT_SUFFIX;
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
     * @param exportContext    The export context.
     * @param exportSourceType The {@link ExportSourceType} of the export.
     * @param exportSourceId   The ID of the export's source object.
     * @param title            The export's title.
     * @param description      The export's description.
     */
    private void exportMainData(ExportContext exportContext, ExportSourceType exportSourceType, String exportSourceId, TranslatableString title, TranslatableString description) {
        ExportMainData exportMainData = new ExportMainData();
        exportMainData.setExportSourceType(exportSourceType);
        exportMainData.setExportSourceId(exportSourceId);
        exportMainData.setTitle(Optional.ofNullable(title).orElse(new TranslatableString()));
        exportMainData.getTitle().setTranslatedValue(null);
        exportMainData.setDescription(Optional.ofNullable(description).orElse(new TranslatableString()));
        exportMainData.getDescription().setTranslatedValue(null);
        writeJsonFile(exportContext.getExportDir().resolve(CONTENT_EXPORT_FILENAME_JSON), exportMainData);
    }

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    private Path exportPropertiesConfiguration(ExportContext exportContext) {
        Path exportFile = exportContext.getExportDir().resolve(PROPERTIES_EXPORT_FILENAME_JSON);
        List<PropertyCategory> categories = configurationService.loadPropertiesConfiguration().getCategories();
        cleanupPropertyCategories(exportContext, categories);
        writeJsonFile(exportFile, categories);
        return exportFile;
    }

    /**
     * Exports properties and tags configuration files.
     *
     * @param exportContext Export parameters.
     */
    private Path exportTagsConfiguration(ExportContext exportContext) {
        Path exportFile = exportContext.getExportDir().resolve(TAGS_EXPORT_FILENAME_JSON);
        List<Tag> tags = configurationService.loadTagsConfiguration().getTags();
        cleanupTags(exportContext, tags);
        writeJsonFile(exportFile, tags);
        return exportFile;
    }

    /**
     * Exports a menu.
     *
     * @param params Parameters of the content export.
     * @param menu   The menu to export.
     */
    private void exportMenu(ExportContext params, Menu menu) {
        cleanupMenu(params, menu);

        writeJsonFile(params.getExportDir().resolve(menu.getId() + MENU_EXPORT_FILE_SUFFIX), menu);

        Optional.ofNullable(menu.getMenuEntries()).orElse(List.of())
                .forEach(menuEntry -> exportMenu(params, menuEntry));

        if (StringUtils.hasText(menu.getTargetPageId())) {
            var pageContent = pageService.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER));
            exportPage(params, menu.getTargetPageId(), pageContent);
        }
    }

    /**
     * Exports a page.
     *
     * @param params      Parameters of the content export.
     * @param pageContent The page to export.
     */
    private void exportPage(ExportContext params, String targetPageId, PageContent pageContent) {
        if (params.getExportConfiguration().isApplyRestrictions() && !pageContent.getRestrictions().isEmpty()) {
            return;
        }

        pageContent.getWidgets().forEach(widget -> {
            if (params.getExportConfiguration().isApplyRestrictions() && !widget.getRestrictions().isEmpty()) {
                return;
            }
            widget.setRestrictions(null);
            exportWidget(params, widget);
        });

        cleanupPage(params, pageContent);

        writeJsonFile(params.getExportDir().resolve(targetPageId + PAGE_EXPORT_FILE_SUFFIX), pageContent);
    }

    /**
     * Exports the supplied widget.
     *
     * @param params Parameters of the content export.
     * @param widget The widget to export.
     */
    private void exportWidget(ExportContext params, Widget widget) {
        widget.getNavigationTitle().setTranslatedValue(null);
        switch (widget) {
            case AvatarWidget avatarWidget -> {
                avatarWidget.getAvatarSubtext().setTranslatedValue(null);
                copyWidgetFile(params, avatarWidget, avatarWidget.getAvatarImage());
            }
            case ImageTextWidget imageTextWidget -> {
                imageTextWidget.getText().setTranslatedValue(null);
                copyWidgetFile(params, imageTextWidget, imageTextWidget.getImage());
            }
            case InfoBoxWidget infoBoxWidget -> {
                infoBoxWidget.getHeading().setTranslatedValue(null);
                infoBoxWidget.getContent().setTranslatedValue(null);
            }
            case PageTitleWidget pageTitleWidget -> {
                pageTitleWidget.getTitle().setTranslatedValue(null);
                copyWidgetFile(params, pageTitleWidget, pageTitleWidget.getBackgroundImage());
            }
            case ItemSearchWidget itemSearchWidget -> {
                itemSearchWidget.getHeading().setTranslatedValue(null);
                itemSearchWidget.getContent().setTranslatedValue(null);
                String searchTerm = itemSearchWidget.getSearchTerm();
                int maxResults = itemSearchWidget.getMaxResults();
                List<Item> searchResult = searchService.search(searchTerm, maxResults);
                if (searchResult != null && !searchResult.isEmpty()) {
                    for (Item item : searchResult) {
                        if (params.getExportConfiguration().isApplyRestrictions() && !item.getRestrictions().isEmpty()) {
                            continue;
                        }
                        exportItem(params, item);
                    }
                    writeJsonFile(params.getExportDir().resolve(widget.getId() + SEARCH_EXPORT_FILE_SUFFIX),
                            searchResult.stream().map(Item::getId).toArray());
                }
            }
            case TextWidget textWidget -> {
                textWidget.getHeading().setTranslatedValue(null);
                textWidget.getContent().setTranslatedValue(null);
            }
            default -> log.info("No export available for widget type: {}", widget.getType());
        }
    }

    /**
     * Exports an item with its media files.
     *
     * @param params The export parameters.
     * @param item   The item to export.
     */
    private void exportItem(ExportContext params, Item item) {
        Path itemExportDir = params.getExportDir().resolve(item.getId());

        // Already exported by another widget / exporter? Skip item...
        if (fileRepository.exists(itemExportDir)) {
            return;
        } else {
            fileRepository.createDirIfRequired(itemExportDir);
        }

        copyItemMediaFiles(params, item, itemExportDir);

        item.getTitle().setTranslatedValue(null);
        item.getDescription().setTranslatedValue(null);

        writeJsonFile(itemExportDir.resolve(ITEM_EXPORT_FILENAME_JSON), item);
    }

    /**
     * Copies the item's media files to the target directory.
     *
     * @param params        Export parameters.
     * @param item          The item to export.
     * @param itemExportDir The item's export directory.
     */
    private void copyItemMediaFiles(ExportContext params, Item item, Path itemExportDir) {
        List<String> images = item.getMediaContent().getImages();
        List<String> models = item.getMediaContent().getModels();

        item.setMediaCreationContent(null); // Not needed in standard exports at the moment.

        Path imagesSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.IMAGES_DIR);
        Path modelsSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.MODELS_DIR);

        if (params.getExportConfiguration().isOptimizeSize()) {
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
     * @param params Export parameters.
     * @param widget The widget to copy files from.
     * @param file   The file to copy.
     */
    private void copyWidgetFile(ExportContext params, Widget widget, String file) {
        if (StringUtils.hasText(file)) {
            Path sourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_FILE_DIR), widget.getId());
            Path targetDir = params.getExportDir().resolve(widget.getId());
            fileRepository.createDirIfRequired(targetDir);
            fileRepository.copy(sourceDir.resolve(file), targetDir.resolve(file));
        }
    }

    /**
     * Cleans the menu up for export.
     *
     * @param params Export parameters.
     * @param menu   The menu to clean up.
     */
    private void cleanupMenu(ExportContext params, Menu menu) {
        cleanupRestrictionsAndTranslations(menu);
        if (menu.getMenuEntries() != null) {
            menu.getMenuEntries().stream()
                    .filter(menuEntry -> {
                        if (params.getExportConfiguration().isApplyRestrictions()) {
                            return menuEntry.getRestrictions().isEmpty();
                        }
                        return true;
                    })
                    .forEach(menuEntry -> {
                        cleanupRestrictionsAndTranslations(menuEntry);
                        menuEntry.setParentId(null);
                        menuEntry.setMenuEntries(menuEntry.getMenuEntries().isEmpty() ? null : menuEntry.getMenuEntries());
                    });
        }
    }

    /**
     * Cleans up property categories for export.
     *
     * @param params             Export parameters.
     * @param propertyCategories The property categories to clean up.
     */
    private void cleanupPropertyCategories(ExportContext params, List<PropertyCategory> propertyCategories) {
        propertyCategories.stream()
                .filter(propertyCategory -> {
                    if (params.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(propertyCategory -> {
                    cleanupRestrictionsAndTranslations(propertyCategory);
                    propertyCategory.getProperties().stream()
                            .filter(property -> {
                                if (params.getExportConfiguration().isApplyRestrictions()) {
                                    return property.getRestrictions().isEmpty();
                                }
                                return true;
                            })
                            .forEach(property -> {
                                cleanupRestrictionsAndTranslations(property);
                                if (property.getValueRange() != null && !property.getValueRange().isEmpty()) {
                                    property.getValueRange().stream()
                                            .filter(propertyValue -> {
                                                if (params.getExportConfiguration().isApplyRestrictions()) {
                                                    return propertyValue.getRestrictions().isEmpty();
                                                }
                                                return true;
                                            })
                                            .forEach(this::cleanupRestrictionsAndTranslations);
                                }
                            });
                });
    }

    /**
     * Cleans up tags for export.
     *
     * @param params Export parameters.
     * @param tags   The tags to clean up.
     */
    private void cleanupTags(ExportContext params, List<Tag> tags) {
        tags.stream()
                .filter(propertyCategory -> {
                    if (params.getExportConfiguration().isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(this::cleanupRestrictionsAndTranslations);
    }

    /**
     * Cleans up a page for export.
     *
     * @param params      Export parameters.
     * @param pageContent The page to clean up.
     */
    private void cleanupPage(ExportContext params, PageContent pageContent) {
        pageContent.setIndexPage(null);
        if (!params.getExportConfiguration().isApplyRestrictions()) {
            pageContent.setRestrictions(null);
        }
    }

    /**
     * Removes restrictions and a translated value from the supplied object.
     *
     * @param translatableRestrictedObject The object to clean up.
     */
    private void cleanupRestrictionsAndTranslations(BaseTranslatableRestrictedObject translatableRestrictedObject) {
        translatableRestrictedObject.setRestrictions(null);
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
