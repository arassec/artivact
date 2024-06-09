package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.PageEntityRepository;
import com.arassec.artivact.backend.persistence.model.PageEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import com.arassec.artivact.backend.service.model.BaseRestrictedObject;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Page;
import com.arassec.artivact.backend.service.model.page.PageContent;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for page handling.
 */
@Slf4j
@Service
public class PageService extends BaseFileService {

    /**
     * Repository for page entities.
     */
    private final PageEntityRepository pageEntityRepository;

    /**
     * The application's {@link FileUtil}.
     */
    @Getter
    private final FileUtil fileUtil;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Path to the widgets data directory.
     */
    private final Path widgetFilesDir;

    /**
     * Creates a new instance.
     *
     * @param pageEntityRepository Repository for page entities.
     * @param fileUtil             The application's {@link FileUtil}.
     * @param objectMapper         The object mapper.
     * @param projectDataProvider  Provider for project data.
     */
    public PageService(PageEntityRepository pageEntityRepository,
                       FileUtil fileUtil,
                       ObjectMapper objectMapper,
                       ProjectDataProvider projectDataProvider) {
        this.pageEntityRepository = pageEntityRepository;
        this.fileUtil = fileUtil;
        this.objectMapper = objectMapper;
        this.widgetFilesDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_FILE_DIR);
    }

    /**
     * Creates a new page.
     *
     * @param restrictions Set of restrictions that should apply to the new page.
     * @return The newly created page.
     */
    public Page createPage(Set<String> restrictions) {
        PageContent pageContent = new PageContent();
        pageContent.setId(UUID.randomUUID().toString());
        pageContent.setRestrictions(restrictions);

        Page page = new Page();
        page.setId(UUID.randomUUID().toString());
        page.setVersion(0);
        page.setPageContent(pageContent);

        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(page.getId());
        pageEntity.setVersion(page.getVersion());
        pageEntity.setContentJson(toJson(pageContent));

        pageEntityRepository.save(pageEntity);

        return page;
    }

    /**
     * Deletes the page with the given ID.
     *
     * @param pageId The page's ID.
     */
    public void deletePage(String pageId) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(pageId);
        if (pageEntityOptional.isPresent()) {
            PageEntity pageEntity = pageEntityOptional.get();
            PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
            pageContent.getWidgets().forEach(widget -> deleteDirAndEmptyParents(fileUtil.getDirFromId(widgetFilesDir, widget.getId())));
            pageEntityRepository.deleteById(pageId);
        }
    }

    /**
     * Updates page restrictions.
     *
     * @param pageId       The page's ID.
     * @param restrictions The new restrictions to apply.
     */
    public void updatePageRestrictions(String pageId, Set<String> restrictions) {
        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();

        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        pageContent.setRestrictions(restrictions);

        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);
    }

    /**
     * Loads the content of the index page.
     *
     * @return The {@link PageContent} of the index page.
     */
    @TranslateResult
    @RestrictResult
    public PageContent loadIndexPageContent() {
        Optional<PageEntity> indexPageOptional = pageEntityRepository.findFirstByIndexPage(true);
        return indexPageOptional.map(this::convertPageEntity).orElseGet(PageContent::new);
    }

    /**
     * Loads the content of the given page.
     *
     * @param pageId The page's ID.
     * @return The {@link PageContent} of the page.
     */
    @TranslateResult
    @RestrictResult
    public PageContent loadPageContent(String pageId) {
        return convertPageEntity(pageEntityRepository.findById(pageId).orElseThrow());
    }

    /**
     * Saves a page's content.
     *
     * @param pageId      The page's ID.
     * @param pageContent The content to save.
     * @return The updated page content.
     */
    @GenerateIds
    @TranslateResult
    @RestrictResult
    public PageContent savePageContent(String pageId, PageContent pageContent) {
        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();

        PageContent existingPageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        List<String> widgetIdsToDelete = existingPageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .collect(Collectors.toList());

        widgetIdsToDelete.removeAll(pageContent.getWidgets().stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        widgetIdsToDelete.forEach(widgetId -> deleteDirAndEmptyParents(fileUtil.getDirFromId(widgetFilesDir, widgetId)));

        pageContent.getWidgets().forEach(widget ->
                getDanglingImages(widget).forEach(imageToDelete -> {
                    try {
                        Files.deleteIfExists(fileUtil.getSubdirFilePath(widgetFilesDir, widget.getId(), null).resolve(imageToDelete));
                        for (ImageSize imageSize : ImageSize.values()) {
                            Files.deleteIfExists(fileUtil.getSubdirFilePath(widgetFilesDir, widget.getId(), null)
                                    .resolve(imageSize.name() + "-" + imageToDelete));
                        }
                    } catch (IOException e) {
                        log.error("Could not delete obsolete widget image from filesystem!", e);
                    }
                })
        );

        pageEntity.setIndexPage(Boolean.TRUE.equals(pageContent.getIndexPage()));
        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);
        return pageContent;
    }

    /**
     * Returns a list of images of a widget that are not referenced by the widget itself, but only existing in the
     * filesystem.
     *
     * @param widget The widget.
     * @return List of unreferenced images.
     */
    private List<String> getDanglingImages(Widget widget) {
        if (widget instanceof FileProcessingWidget fileProcessingWidget) {
            List<String> imagesInWidget = fileProcessingWidget.usedFiles();
            List<String> allImagesInFolder = getFiles(fileUtil.getDirFromId(widgetFilesDir, widget.getId()), null);
            allImagesInFolder.removeAll(imagesInWidget);
            return allImagesInFolder;
        }
        return List.of();
    }

    /**
     * Saves a file for a given widget in the filesystem.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param file     The file to save.
     * @return The name of the saved file.
     */
    public String saveFile(String pageId, String widgetId, MultipartFile file) {

        String filename = saveFile(widgetFilesDir, widgetId, file);

        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();
        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);
        pageContent.getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename);
            }
        });

        pageEntity.setContentJson(toJson(pageContent));
        pageEntityRepository.save(pageEntity);

        return filename;
    }

    /**
     * Loads a widget's file from the filesystem.
     *
     * @param widgetId   The widget's ID.
     * @param filename   The filename.
     * @param targetSize The desired target size (if an image is loaded).
     * @return The file as {@link FileSystemResource}.
     */
    public FileSystemResource loadFile(String widgetId, String filename, ImageSize targetSize) {
        return loadFile(widgetFilesDir, widgetId, filename, targetSize);
    }

    /**
     * Converts a {@link PageEntity} into its {@link PageContent}.
     *
     * @param pageEntity The entity to convert.
     * @return The page's content.
     */
    private PageContent convertPageEntity(PageEntity pageEntity) {
        PageContent pageContent = fromJson(pageEntity.getContentJson(), PageContent.class);

        PageContent result = new PageContent();
        result.setId(pageContent.getId());
        result.setIndexPage(pageContent.getIndexPage());
        result.setWidgets(pageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .toList());

        return result;
    }

}
