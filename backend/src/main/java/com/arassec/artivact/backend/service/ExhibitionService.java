package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ExhibitionEntityRepository;
import com.arassec.artivact.backend.persistence.model.ExhibitionEntity;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.misc.ProgressMonitor;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.arassec.artivact.backend.service.model.BaseRestrictedObject;
import com.arassec.artivact.backend.service.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.exhibition.Exhibition;
import com.arassec.artivact.backend.service.model.exhibition.Tool;
import com.arassec.artivact.backend.service.model.exhibition.Topic;
import com.arassec.artivact.backend.service.model.exhibition.tool.ItemsTool;
import com.arassec.artivact.backend.service.model.exhibition.tool.TitleTool;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.model.menu.Menu;
import com.arassec.artivact.backend.service.model.page.PageContent;
import com.arassec.artivact.backend.service.model.page.widget.PageTitleWidget;
import com.arassec.artivact.backend.service.model.page.widget.SearchBasedWidget;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for exhibition creation and management.
 */
@Slf4j
@Service
@Transactional
public class ExhibitionService extends BaseFileService {

    /**
     * File suffix for JSON files inside the exhibition's ZIP file.
     */
    private static final String JSON_FILE_SUFFIX = ".artivact.json";

    /**
     * File suffix for the exhibitions ZIP file.
     */
    private static final String EXHIBITION_FILE_SUFFIX = ".artivact-exhibition.zip";

    /**
     * Repository for exhibition entities.
     */
    private final ExhibitionEntityRepository exhibitionEntityRepository;

    /**
     * Service for configuration handling.
     */
    private final ConfigurationService configurationService;

    /**
     * Service for item searches.
     */
    private final SearchService searchService;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Service for item handling.
     */
    private final ItemService itemService;

    /**
     * The application's file util.
     */
    @Getter
    private final FileUtil fileUtil;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Path to the exhibitions export directory.
     */
    private final Path exhibitionsDir;

    /**
     * Executor service for long-running background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * The progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Creates a new instance.
     *
     * @param exhibitionEntityRepository Repository for exhibition entities.
     * @param configurationService       Service for configuration handling.
     * @param searchService              Service for item searches.
     * @param pageService                Service for page handling.
     * @param itemService                Service for item handling.
     * @param fileUtil                   The application's file util.
     * @param objectMapper               The object mapper for exports.
     * @param projectDataProvider        Provider of project data.
     */
    public ExhibitionService(ExhibitionEntityRepository exhibitionEntityRepository,
                             ConfigurationService configurationService,
                             SearchService searchService,
                             PageService pageService,
                             ItemService itemService,
                             FileUtil fileUtil,
                             ObjectMapper objectMapper,
                             ProjectDataProvider projectDataProvider) {
        this.exhibitionEntityRepository = exhibitionEntityRepository;
        this.configurationService = configurationService;
        this.searchService = searchService;
        this.pageService = pageService;
        this.itemService = itemService;
        this.fileUtil = fileUtil;
        this.objectMapper = objectMapper;

        this.exhibitionsDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXHIBITIONS_DIR);
        if (!Files.exists(exhibitionsDir)) {
            try {
                Files.createDirectories(exhibitionsDir);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create exhibitionsDir directory!", e);
            }
        }
    }

    /**
     * Loads all available exhibitions.
     *
     * @return List of available {@link Exhibition}s.
     */
    public List<Exhibition> loadAll() {
        List<Exhibition> result = new LinkedList<>();
        exhibitionEntityRepository.findAll()
                .forEach(exhibitionEntity -> {
                    Exhibition exhibition = fromJson(exhibitionEntity.getContentJson(), Exhibition.class);
                    exhibition.setVersion(exhibitionEntity.getVersion());
                    result.add(exhibition);
                });
        return result;
    }

    /**
     * Deletes the exhibition with the given ID.
     *
     * @param exhibitionId The exhibition's ID.
     */
    public void delete(String exhibitionId) {
        exhibitionEntityRepository.deleteById(exhibitionId);
        try {
            Files.deleteIfExists(exhibitionsDir.resolve(exhibitionId + EXHIBITION_FILE_SUFFIX).toAbsolutePath());
        } catch (IOException e) {
            throw new ArtivactException("Could not delete exhibition file!", e);
        }
    }

    /**
     * Creates or updates an existing exhibition.
     *
     * @param exhibitionId      The ID of an existing exhibition or {@code null} for new ones.
     * @param title             The exhibition's title.
     * @param description       The exhibition's description.
     * @param referencedMenuIds The IDs of menus that must be used as source for exhibition creation.
     */
    public synchronized void createOrUpdate(String exhibitionId, TranslatableString title, TranslatableString description, List<String> referencedMenuIds) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "createOrUpdate");

        if (!StringUtils.hasText(exhibitionId)) {
            exhibitionId = UUID.randomUUID().toString();
        }

        // Cleanup menus that might have been removed since last update:
        List<Menu> existingMenus = configurationService.loadTranslatedMenus();
        referencedMenuIds.retainAll(existingMenus.stream()
                .map(BaseTranslatableRestrictedObject::getId)
                .toList());

        final String id = exhibitionId;

        executorService.submit(() -> {
            try {
                ExhibitionEntity exhibitionEntity = exhibitionEntityRepository.findById(id).orElse(new ExhibitionEntity());

                Exhibition exhibition;
                if (StringUtils.hasText(exhibitionEntity.getContentJson())) {
                    exhibition = fromJson(exhibitionEntity.getContentJson(), Exhibition.class);
                    exhibition.setVersion(exhibitionEntity.getVersion());
                } else {
                    exhibition = new Exhibition();
                    exhibition.setVersion(0);
                }

                exhibition.setId(id);

                exhibition.setTitle(title);
                exhibition.getTitle().setTranslatedValue(null);
                exhibition.setDescription(description);
                exhibition.getDescription().setTranslatedValue(null);
                exhibition.setLastModified(Instant.now());
                exhibition.setReferencedMenuIds(referencedMenuIds);

                exhibition.setPropertyCategories(configurationService.loadTranslatedProperties());
                exhibition.getPropertyCategories().forEach(propertyCategory -> {
                    propertyCategory.setTranslatedValue(null);
                    propertyCategory.setRestrictions(null);
                    propertyCategory.getProperties().forEach(property -> {
                        property.setTranslatedValue(null);
                        property.setRestrictions(null);
                    });
                });
                exhibition.setTags(configurationService.loadTagsConfiguration().getTags());
                exhibition.getTags().forEach(tag -> {
                    tag.setTranslatedValue(null);
                    tag.setRestrictions(null);
                });

                exhibition.getTopics().clear();
                referencedMenuIds.forEach(menuId -> exhibition.getTopics().add(createTopicFromMenu(existingMenus, menuId)));

                createExhibitionFile(exhibition);

                exhibitionEntity.setVersion(exhibition.getVersion());
                exhibitionEntity.setId(exhibition.getId());
                exhibitionEntity.setContentJson(toJson(exhibition));

                exhibitionEntityRepository.save(exhibitionEntity);

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("createOrUpdateFailed", e);
                log.error("Error during exhibition processing!", e);
            }
        });
    }

    /**
     * Creates an exhibition {@link Topic} from the provided menu.
     *
     * @param menus  All available menus.
     * @param menuId The ID of the menu to create the topic from.
     * @return The newly created topic.
     */
    private Topic createTopicFromMenu(List<Menu> menus, String menuId) {
        Topic topic = new Topic();

        if (!StringUtils.hasText(menuId)) {
            return topic;
        }

        Optional<Menu> selectedMenuOptional = menus.stream()
                .filter(menu -> menu.getId().equals(menuId))
                .findFirst();

        if (selectedMenuOptional.isEmpty()) {
            selectedMenuOptional = menus.stream()
                    .flatMap(menu -> menu.getMenuEntries().stream())
                    .filter(menu -> menu.getId().equals(menuId))
                    .findFirst();
        }

        Menu selectedMenu = selectedMenuOptional.orElseThrow();

        TranslatableString topicTitle = new TranslatableString();
        topicTitle.setValue(selectedMenu.getValue());
        topicTitle.setTranslations(selectedMenu.getTranslations());
        topic.setTitle(topicTitle);

        if (StringUtils.hasText(selectedMenu.getTargetPageId())) {
            PageContent pageContent = pageService.loadPageContent(selectedMenu.getTargetPageId());
            pageContent.getWidgets().forEach(widget -> {
                switch (widget.getType()) {
                    case PAGE_TITLE ->
                            createTitleTool(((PageTitleWidget) widget)).ifPresent(tool -> topic.getTools().add(tool));
                    case ITEM_CAROUSEL, ITEM_SEARCH ->
                            createItemsTool(((SearchBasedWidget) widget)).ifPresent(tool -> topic.getTools().add(tool));
                    default ->
                            log.info("No conversion from widget type '{}' to exhibition tool defined!", widget.getType());
                }
            });
        }

        return topic;
    }

    /**
     * Converts a {@link PageTitleWidget} into an exhibition's {@link TitleTool}.
     *
     * @param pageTitleWidget The widget to use as input.
     * @return A newly created title tool for the exhibition.
     */
    private Optional<Tool> createTitleTool(PageTitleWidget pageTitleWidget) {
        if (!StringUtils.hasText(pageTitleWidget.getTitle().getValue())
                && !StringUtils.hasText(pageTitleWidget.getBackgroundImage())) {
            return Optional.empty();
        }

        TitleTool tool = new TitleTool();
        tool.setTitle(pageTitleWidget.getTitle());
        tool.getTitle().setTranslatedValue(null);
        return Optional.of(tool);
    }

    /**
     * Converts a {@link SearchBasedWidget} into an exhibition's {@link ItemsTool}.
     *
     * @param searchBasedWidget The widget to use as input.
     * @return A newly created items tool for the exhibition.
     */
    private Optional<Tool> createItemsTool(SearchBasedWidget searchBasedWidget) {
        ItemsTool tool = new ItemsTool();

        if (!StringUtils.hasText(searchBasedWidget.getSearchTerm())) {
            return Optional.empty();
        }

        List<Item> searchResult = searchService.search(searchBasedWidget.getSearchTerm(), searchBasedWidget.getMaxResults());

        tool.setItemIds(searchResult.stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        return Optional.of(tool);
    }

    /**
     * Creates the exhibition's main JSON file.
     *
     * @param exhibition The exhibition.
     */
    private void createExhibitionFile(Exhibition exhibition) {
        Path exhibitionDir = exhibitionsDir.resolve(exhibition.getId());

        fileUtil.deleteDir(exhibitionDir);
        fileUtil.createDirIfRequired(exhibitionDir);

        Path exhibitionJsonFile = exhibitionDir.resolve(exhibition.getId() + JSON_FILE_SUFFIX);
        try {
            objectMapper.writeValue(exhibitionJsonFile.toAbsolutePath().toFile(), exhibition);
        } catch (IOException e) {
            throw new ArtivactException("Could not create exhibition JSON file!", e);
        }

        exhibition.getTopics().forEach(topic -> topic.getTools().forEach(tool -> {
            if (tool instanceof ItemsTool itemsTool) {
                itemsTool.getItemIds().forEach(itemId -> {
                    Item item = itemService.load(itemId);
                    item.setMediaCreationContent(null); // We don't need this in the JSON file!

                    Path itemDir = exhibitionDir.resolve(item.getId());
                    fileUtil.createDirIfRequired(itemDir);

                    Path itemJsonFile = itemDir.resolve(item.getId() + JSON_FILE_SUFFIX);
                    try {
                        objectMapper.writeValue(itemJsonFile.toAbsolutePath().toFile(), item);
                    } catch (IOException e) {
                        throw new ArtivactException("Could not create item JSON file!", e);
                    }

                    item.getMediaContent().getImages()
                            .forEach(image -> itemService.copyFile(itemId, image, ProjectDataProvider.IMAGES_DIR, itemDir));

                    item.getMediaContent().getModels()
                            .forEach(model -> itemService.copyFile(itemId, model, ProjectDataProvider.MODELS_DIR, itemDir));
                });
            }
        }));

        ZipUtil.pack(exhibitionDir.toAbsolutePath().toFile(),
                exhibitionsDir.resolve(exhibition.getId() + EXHIBITION_FILE_SUFFIX).toAbsolutePath().toFile());

        fileUtil.deleteDir(exhibitionDir);
    }

}
