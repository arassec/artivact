package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ExhibitionEntityRepository;
import com.arassec.artivact.backend.persistence.model.ExhibitionEntity;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.BaseRestrictedObject;
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
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Slf4j
@Service
@Transactional
public class ExhibitionService extends BaseFileService {

    private static final String JSON_FILE_SUFFIX = ".artivact.json";

    private static final String EXHIBITION_FILE_SUFFIX = ".artivact-exhibition.zip";

    private final ExhibitionEntityRepository exhibitionEntityRepository;

    private final ConfigurationService configurationService;

    private final SearchService searchService;

    private final PageService pageService;

    private final ItemService itemService;

    @Getter
    private final FileUtil fileUtil;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path exhibitionsDir;

    public ExhibitionService(ExhibitionEntityRepository exhibitionEntityRepository,
                             ConfigurationService configurationService,
                             SearchService searchService,
                             PageService pageService,
                             ItemService itemService,
                             FileUtil fileUtil,
                             @Qualifier("exportObjectMapper") ObjectMapper exportObjectMapper,
                             ProjectDataProvider projectDataProvider) {
        this.exhibitionEntityRepository = exhibitionEntityRepository;
        this.configurationService = configurationService;
        this.searchService = searchService;
        this.pageService = pageService;
        this.itemService = itemService;
        this.fileUtil = fileUtil;
        this.objectMapper = exportObjectMapper;

        this.exhibitionsDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.EXHIBITIONS_DIR);
        if (!Files.exists(exhibitionsDir)) {
            try {
                Files.createDirectories(exhibitionsDir);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create exhibitionsDir directory!", e);
            }
        }
    }

    public List<Exhibition> loadAll() {
        List<Exhibition> result = new LinkedList<>();
        exhibitionEntityRepository.findAll()
                .forEach(exhibitionEntity -> result.add(fromJson(exhibitionEntity.getContentJson(), Exhibition.class)));
        return result;
    }

    public Exhibition save(String exhibitionId, TranslatableString title, TranslatableString description, List<String> menuIds) {

        if (!StringUtils.hasText(exhibitionId)) {
            exhibitionId = UUID.randomUUID().toString();
        }

        ExhibitionEntity exhibitionEntity = exhibitionEntityRepository.findById(exhibitionId).orElse(new ExhibitionEntity());

        Exhibition exhibition;
        if (StringUtils.hasText(exhibitionEntity.getContentJson())) {
            exhibition = fromJson(exhibitionEntity.getContentJson(), Exhibition.class);
        } else {
            exhibition = new Exhibition();
            exhibition.setVersion(0);
        }

        exhibition.setId(exhibitionId);

        exhibition.setTitle(title);
        exhibition.getTitle().setTranslatedValue(null);
        exhibition.setDescription(description);
        exhibition.getDescription().setTranslatedValue(null);
        exhibition.setLastModified(Instant.now());
        exhibition.setReferencedMenuIds(menuIds);

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
        menuIds.forEach(menuId -> exhibition.getTopics()
                .add(createTopicFromMenu(configurationService.loadTranslatedMenus(), menuId)));

        createExhibitionFile(exhibition);

        exhibitionEntity.setVersion(exhibition.getVersion());
        exhibitionEntity.setId(exhibition.getId());
        exhibitionEntity.setContentJson(toJson(exhibition));

        exhibitionEntityRepository.save(exhibitionEntity);

        return exhibition;
    }

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
                    case PAGE_TITLE -> topic.getTools().add(createTitleTool(((PageTitleWidget) widget)));
                    case ITEM_CAROUSEL, ITEM_SEARCH ->
                            topic.getTools().add(createItemsTool(((SearchBasedWidget) widget)));
                    default ->
                            log.info("No conversion from widget type '{}' to exhibition tool defined!", widget.getType());
                }
            });
        }

        return topic;
    }

    private Tool createTitleTool(PageTitleWidget pageTitleWidget) {
        TitleTool tool = new TitleTool();
        tool.setTitle(pageTitleWidget.getTitle());
        tool.getTitle().setTranslatedValue(null);
        return tool;
    }

    private Tool createItemsTool(SearchBasedWidget searchBasedWidget) {
        ItemsTool tool = new ItemsTool();

        List<Item> searchResult = searchService.search(searchBasedWidget.getSearchTerm(), searchBasedWidget.getMaxResults());

        tool.setItemIds(searchResult.stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        return tool;
    }

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

    public void delete(String exhibitionId) {
        exhibitionEntityRepository.deleteById(exhibitionId);
        try {
            Files.deleteIfExists(exhibitionsDir.resolve(exhibitionId + EXHIBITION_FILE_SUFFIX).toAbsolutePath());
        } catch (IOException e) {
            throw new ArtivactException("Could not delete exhibition file!", e);
        }
    }

}
