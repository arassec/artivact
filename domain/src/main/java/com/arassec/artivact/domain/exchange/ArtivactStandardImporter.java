package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ContentSource;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.item.MediaCreationContent;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.domain.service.ItemService;
import com.arassec.artivact.domain.service.MenuService;
import com.arassec.artivact.domain.service.PageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.*;

/**
 * Standard {@link ArtivactImporter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtivactStandardImporter implements ArtivactImporter {

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
     * Service for item handling.
     */
    private final MenuService menuService;

    /**
     * Service for item handling.
     */
    private final ItemService itemService;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Service for configuration handling.
     */
    private final ConfigurationService configurationService;

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
                importMenu(importContext, exchangeMainData.getSourceId(), true);
            } else if (ContentSource.ITEM.equals(exchangeMainData.getContentSource())) {
                importItem(importContext, exchangeMainData.getSourceId());
            } else {
                throw new ArtivactException("Unknown content source: " + exchangeMainData.getContentSource());
            }

            importPropertiesConfiguration(importContext);

            importTagsConfiguration(importContext);

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
                importPropertiesConfiguration(importContext);
                importTagsConfiguration(importContext);
                importMenu(importContext, exchangeMainData.getSourceId(), true);
            }

            fileRepository.copy(collectionExportFile, projectDataProvider.getProjectRoot()
                    .resolve(ProjectDataProvider.EXPORT_DIR)
                    .resolve(collectionExportFile.getFileName()));

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

    /**
     * Imports the properties configuration.
     *
     * @param importContext The import context.
     * @throws JsonProcessingException In case of parsing errors.
     */
    private void importPropertiesConfiguration(ImportContext importContext) throws JsonProcessingException {
        Path propertiesConfigurationJson = importContext.getImportDir().resolve(PROPERTIES_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(propertiesConfigurationJson)) {
            PropertiesConfiguration propertiesConfiguration = objectMapper.readValue(fileRepository.read(propertiesConfigurationJson), PropertiesConfiguration.class);
            configurationService.savePropertiesConfiguration(propertiesConfiguration);
        }
    }

    /**
     * Imports the tags configuration.
     *
     * @param importContext The import context.
     * @throws JsonProcessingException In case of parsing errors.
     */
    private void importTagsConfiguration(ImportContext importContext) throws JsonProcessingException {
        Path tagsConfigurationJson = importContext.getImportDir().resolve(TAGS_EXCHANGE_FILENAME_JSON);
        if (fileRepository.exists(tagsConfigurationJson)) {
            TagsConfiguration tagsConfiguration = objectMapper.readValue(fileRepository.read(tagsConfigurationJson), TagsConfiguration.class);
            configurationService.saveTagsConfiguration(tagsConfiguration);
        }
    }

    /**
     * Imports a menu.
     *
     * @param importContext The import context.
     * @param menuId        The menu's ID.
     */
    private void importMenu(ImportContext importContext, String menuId, boolean saveMenu) {
        Path menuJson = importContext.getImportDir().resolve(menuId + MENU_EXCHANGE_FILE_SUFFIX);
        try {
            Menu menu = objectMapper.readValue(fileRepository.read(menuJson), Menu.class);
            if (!menu.getMenuEntries().isEmpty()) {
                menu.getMenuEntries().forEach(menuEntry -> {
                    menuEntry.setParentId(menuId);
                    importMenu(importContext, menuEntry.getId(), false);
                });
            } else if (StringUtils.hasText(menu.getTargetPageId())) {
                importPage(importContext, menu.getTargetPageId());
            }

            if (saveMenu) {
                Optional<Path> coverPictureOptional = fileRepository.list(importContext.getImportDir()).stream()
                        .filter(file -> file.getFileName().toString().startsWith("cover-picture"))
                        .findFirst();

                if (coverPictureOptional.isPresent()) {
                    Path coverPictureTargetDir = fileRepository.getSubdirFilePath(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.MENUS_DIR), menuId, null);
                    fileRepository.createDirIfRequired(coverPictureTargetDir);
                    fileRepository.copy(coverPictureOptional.get(), coverPictureTargetDir.resolve(coverPictureOptional.get().getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                }

                // When importing exported sub-menus the parent is set to null and the sub-menu is imported as regular menu.
                menu.setParentId(null);

                menuService.saveMenu(menu);
            }
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import menu!", e);
        }
    }

    /**
     * Imports a page.
     *
     * @param importContext The import context.
     * @param pageId        The page's ID.
     */
    private void importPage(ImportContext importContext, String pageId) {
        Path pageContentJson = importContext.getImportDir().resolve(pageId + PAGE_EXCHANGE_FILE_SUFFIX);

        try {
            PageContent pageContent = objectMapper.readValue(fileRepository.read(pageContentJson), PageContent.class);

            pageContent.getWidgets().forEach(widget -> {
                // Import the widget:
                Path widgetSource = importContext.getImportDir().resolve(widget.getId());
                Path widgetTarget = projectDataProvider.getProjectRoot()
                        .resolve(ProjectDataProvider.WIDGETS_DIR)
                        .resolve(fileRepository.getSubDir(widget.getId(), 0))
                        .resolve(fileRepository.getSubDir(widget.getId(), 1))
                        .resolve(widget.getId());
                fileRepository.copyDir(widgetSource, widgetTarget);

                // Import Items:
                if (widget instanceof ItemSearchWidget itemSearchWidget) {
                    Path searchResultJson = importContext.getImportDir().resolve(itemSearchWidget.getId() + SEARCH_RESULT_FILE_SUFFIX);
                    if (fileRepository.exists(searchResultJson)) {
                        try {
                            List<String> itemIds = objectMapper.readValue(fileRepository.read(searchResultJson), new TypeReference<>() {
                            });
                            itemIds.forEach(itemId -> importItem(importContext, itemId));
                        } catch (JsonProcessingException e) {
                            throw new ArtivactException("Could not read search result!", e);
                        }
                    }
                }
            });

            pageService.savePageContent(pageId, Set.of(), pageContent);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import page!", e);
        }
    }

    /**
     * Imports an item.
     *
     * @param importContext The import context.
     * @param itemId        The item's ID.
     */
    private void importItem(ImportContext importContext, String itemId) {
        Path itemDir = importContext.getImportDir().resolve(itemId);
        String itemJson = fileRepository.read(itemDir.resolve(ITEM_EXCHANGE_FILENAME_JSON));

        try {
            Item item = objectMapper.readValue(itemJson, Item.class);

            item.setMediaCreationContent(new MediaCreationContent());

            item.getMediaContent().getImages()
                    .forEach(image -> itemService.saveImage(item.getId(), image, fileRepository.readStream(itemDir.resolve(image)), true));

            item.getMediaContent().getModels()
                    .forEach(model -> itemService.saveModel(item.getId(), model, fileRepository.readStream(itemDir.resolve(model)), true));

            itemService.save(item);
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import item!", e);
        }
    }

}
