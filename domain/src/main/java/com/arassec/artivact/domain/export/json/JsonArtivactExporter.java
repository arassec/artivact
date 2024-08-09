package com.arassec.artivact.domain.export.json;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.export.ArtivactExporter;
import com.arassec.artivact.domain.export.json.model.ContentExportFile;
import com.arassec.artivact.domain.export.model.ExportParams;
import com.arassec.artivact.domain.export.model.ExportType;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An exporter that writes JSON files.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonArtivactExporter implements ArtivactExporter {

    /**
     * File suffix for exported menus.
     */
    public static final String MENU_EXPORT_FILE_SUFFIX = ".artivact.menu.json";

    /**
     * File suffix for exported pages.
     */
    public static final String PAGE_EXPORT_FILE_SUFFIX = ".artivact.page-content.json";

    /**
     * File suffix for exported items.
     */
    public static final String ITEM_EXPORT_FILE_SUFFIX = ".artivact.item.json";

    /**
     * File suffix for exported search results.
     */
    public static final String SEARCH_EXPORT_FILE_SUFFIX = ".artivact.search-result.json";

    /**
     * The service for configuration handling.
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
    public ExportType supports() {
        return ExportType.JSON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(ExportParams params, Menu menu, ProgressMonitor progressMonitor) {

        ContentExportFile contentExportFile = new ContentExportFile();

        contentExportFile.setTitle(menu.getExportTitle());
        contentExportFile.getTitle().setTranslatedValue(null);
        contentExportFile.setDescription(menu.getExportDescription());
        contentExportFile.getDescription().setTranslatedValue(null);

        contentExportFile.setMenuId(menu.getId());

        contentExportFile.setPropertyCategories(configurationService.loadPropertiesConfiguration().getCategories());
        cleanupPropertyCategories(params, contentExportFile.getPropertyCategories());

        contentExportFile.setTags(configurationService.loadTagsConfiguration().getTags());
        cleanupTags(params, contentExportFile.getTags());

        writeJsonFile(params.getContentExportDir().resolve(CONTENT_EXPORT_FILE_SUFFIX_JSON), contentExportFile);

        exportMenu(params, menu);
    }

    /**
     * Exports a menu.
     *
     * @param params Parameters of the content export.
     * @param menu   The menu to export.
     */
    private void exportMenu(ExportParams params, Menu menu) {
        cleanupMenu(params, menu);

        writeJsonFile(params.getContentExportDir().resolve(menu.getId() + MENU_EXPORT_FILE_SUFFIX), menu);

        Optional.ofNullable(menu.getMenuEntries()).orElse(List.of())
                .forEach(menuEntry -> exportMenu(params, menuEntry));

        if (StringUtils.hasText(menu.getTargetPageId())) {
            exportPage(params, pageService.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER)));
        }
    }

    /**
     * Exports a page.
     *
     * @param params      Parameters of the content export.
     * @param pageContent The page to export.
     */
    private void exportPage(ExportParams params, PageContent pageContent) {
        if (params.isApplyRestrictions() && !pageContent.getRestrictions().isEmpty()) {
            return;
        }

        pageContent.getWidgets().forEach(widget -> {
            if (params.isApplyRestrictions() && !widget.getRestrictions().isEmpty()) {
                return;
            }
            widget.setRestrictions(null);
            exportWidget(params, widget);
        });

        cleanupPage(params, pageContent);

        writeJsonFile(params.getContentExportDir().resolve(pageContent.getId() + PAGE_EXPORT_FILE_SUFFIX), pageContent);
    }

    /**
     * Exports the supplied widget.
     *
     * @param params Parameters of the content export.
     * @param widget The widget to export.
     */
    private void exportWidget(ExportParams params, Widget widget) {
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
            case SearchBasedWidget searchBasedWidget -> {
                String searchTerm = searchBasedWidget.getSearchTerm();
                int maxResults = searchBasedWidget.getMaxResults();
                List<Item> searchResult = searchService.search(searchTerm, maxResults);
                if (searchResult != null && !searchResult.isEmpty()) {
                    for (Item item : searchResult) {
                        if (params.isApplyRestrictions() && !item.getRestrictions().isEmpty()) {
                            continue;
                        }
                        exportItem(params, item);
                    }

                    writeJsonFile(params.getContentExportDir().resolve(widget.getId() + SEARCH_EXPORT_FILE_SUFFIX),
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
    private void exportItem(ExportParams params, Item item) {
        Path itemExportDir = params.getContentExportDir().resolve(item.getId());

        // Already exported by another widget / exporter? Skip item...
        if (fileRepository.exists(itemExportDir)) {
            return;
        } else {
            fileRepository.createDirIfRequired(itemExportDir);
        }

        copyItemMediaFiles(params, item, itemExportDir);

        item.getTitle().setTranslatedValue(null);
        item.getDescription().setTranslatedValue(null);

        writeJsonFile(itemExportDir.resolve(item.getId() + ITEM_EXPORT_FILE_SUFFIX), item);
    }

    /**
     * Copies the item's media files to the target directory.
     *
     * @param params        Export parameters.
     * @param item          The item to export.
     * @param itemExportDir The item's export directory.
     */
    private void copyItemMediaFiles(ExportParams params, Item item, Path itemExportDir) {
        List<String> images = item.getMediaContent().getImages();
        List<String> models = item.getMediaContent().getModels();

        item.setMediaCreationContent(null); // Not needed in export at the moment!

        Path imagesSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.IMAGES_DIR);
        Path modelsSourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.ITEMS_DIR), item.getId()).resolve(ProjectDataProvider.MODELS_DIR);

        if (params.isOptimizeSize()) {
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
    private void copyWidgetFile(ExportParams params, Widget widget, String file) {
        if (StringUtils.hasText(file)) {
            Path sourceDir = fileRepository.getDirFromId(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_FILE_DIR), widget.getId());
            Path targetDir = params.getContentExportDir().resolve(widget.getId());
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
    private void cleanupMenu(ExportParams params, Menu menu) {
        cleanupRestrictionsAndTranslations(menu);
        if (menu.getMenuEntries() != null) {
            menu.getMenuEntries().stream()
                    .filter(menuEntry -> {
                        if (params.isApplyRestrictions()) {
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
    private void cleanupPropertyCategories(ExportParams params, List<PropertyCategory> propertyCategories) {
        propertyCategories.stream()
                .filter(propertyCategory -> {
                    if (params.isApplyRestrictions()) {
                        return propertyCategory.getRestrictions().isEmpty();
                    }
                    return true;
                })
                .forEach(propertyCategory -> {
                    cleanupRestrictionsAndTranslations(propertyCategory);
                    propertyCategory.getProperties().stream()
                            .filter(property -> {
                                if (params.isApplyRestrictions()) {
                                    return property.getRestrictions().isEmpty();
                                }
                                return true;
                            })
                            .forEach(property -> {
                                cleanupRestrictionsAndTranslations(property);
                                if (property.getValueRange() != null && !property.getValueRange().isEmpty()) {
                                    property.getValueRange().stream()
                                            .filter(propertyValue -> {
                                                if (params.isApplyRestrictions()) {
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
    private void cleanupTags(ExportParams params, List<Tag> tags) {
        tags.stream()
                .filter(propertyCategory -> {
                    if (params.isApplyRestrictions()) {
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
    private void cleanupPage(ExportParams params, PageContent pageContent) {
        pageContent.setIndexPage(null);
        if (!params.isApplyRestrictions()) {
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
