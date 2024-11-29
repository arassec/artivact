package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.BaseRestrictedObject;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.page.FileProcessingWidget;
import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.PageRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for page handling.
 */
@Slf4j
@Service
@Transactional
public class PageService extends BaseFileService {

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

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
     * @param pageRepository      Repository for pages.
     * @param fileRepository      The application's {@link FileRepository}.
     * @param objectMapper        The object mapper.
     * @param projectDataProvider Provider for project data.
     */
    public PageService(PageRepository pageRepository,
                       FileRepository fileRepository,
                       ObjectMapper objectMapper,
                       ProjectDataProvider projectDataProvider) {
        this.pageRepository = pageRepository;
        this.fileRepository = fileRepository;
        this.objectMapper = objectMapper;
        this.widgetFilesDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.WIDGETS_DIR);
    }

    /**
     * Creates a new page.
     *
     * @param restrictions Set of restrictions that should apply to the new page.
     * @return The newly created page.
     */
    public Page createPage(Set<String> restrictions) {
        String pageId = UUID.randomUUID().toString();

        PageContent pageContent = new PageContent();
        pageContent.setId(pageId);
        pageContent.setRestrictions(restrictions);

        Page page = new Page();
        page.setId(pageId);
        page.setVersion(0);
        page.setPageContent(pageContent);

        pageRepository.save(page);

        return page;
    }

    /**
     * Deletes the page with the given ID.
     *
     * @param pageId The page's ID.
     */
    public void deletePage(String pageId) {
        Optional<Page> pageOptional = pageRepository.deleteById(pageId);
        if (pageOptional.isPresent()) {
            pageOptional.get().getPageContent().getWidgets().forEach(
                    widget -> deleteDirAndEmptyParents(fileRepository.getDirFromId(widgetFilesDir, widget.getId()))
            );
            pageRepository.deleteById(pageId);
        }
    }

    /**
     * Updates page restrictions.
     *
     * @param pageId       The page's ID.
     * @param restrictions The new restrictions to apply.
     */
    public void updatePageRestrictions(String pageId, Set<String> restrictions) {
        Page page = pageRepository.findById(pageId).orElseThrow();
        page.getPageContent().setRestrictions(restrictions);
        pageRepository.save(page);
    }

    /**
     * Loads the content of the index page.
     *
     * @return The {@link PageContent} of the index page.
     */
    @TranslateResult
    @RestrictResult
    public PageContent loadIndexPageContent(Set<String> roles) {
        PageContent pageContent = pageRepository.findIndexPage().getPageContent();
        computeEditable(pageContent, roles);
        return pageContent;
    }

    /**
     * Loads the content of the given page.
     *
     * @param pageId The page's ID.
     * @param roles  The available roles.
     * @return The {@link PageContent} of the page.
     */
    public PageContent loadPageContent(String pageId, Set<String> roles) {
        Page page = pageRepository.findById(pageId).orElseThrow();
        computeEditable(page.getPageContent(), roles);
        return page.getPageContent();
    }

    /**
     * Loads the content of the given page and applies translations and restrictions.
     *
     * @param pageId The page's ID.
     * @param roles  The available roles.
     * @return The {@link PageContent} of the page.
     */
    @TranslateResult
    @RestrictResult
    public PageContent loadTranslatedRestrictedPageContent(String pageId, Set<String> roles) {
        Page page = pageRepository.findById(pageId).orElseThrow();
        computeEditable(page.getPageContent(), roles);
        return page.getPageContent();
    }

    /**
     * Saves a page's content.
     *
     * @param pageId      The page's ID.
     * @param roles       The available roles.
     * @param pageContent The content to save.
     * @return The updated page content.
     */
    @GenerateIds
    @TranslateResult
    @RestrictResult
    public PageContent savePageContent(String pageId, Set<String> roles, PageContent pageContent) {
        Optional<Page> pageOptional = pageRepository.findById(pageId);

        Page page;
        if (pageOptional.isPresent()) {
            page = pageOptional.get();
        } else {
            // Importing content from a previous export:
            page = new Page();
            page.setId(pageId);
            page.setVersion(0);
            page.setPageContent(pageContent);
        }

        PageContent existingPageContent = page.getPageContent();
        computeEditable(existingPageContent, roles);

        if (!existingPageContent.isEditable()) {
            throw new ArtivactException("Page can not be edited by the current user!");
        }

        List<String> widgetIdsToDelete = existingPageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .collect(Collectors.toList());

        widgetIdsToDelete.removeAll(pageContent.getWidgets().stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        widgetIdsToDelete.forEach(widgetId -> deleteDirAndEmptyParents(fileRepository.getDirFromId(widgetFilesDir, widgetId)));

        pageContent.getWidgets().forEach(widget ->
                getDanglingImages(widget).forEach(imageToDelete -> {
                    fileRepository.delete(fileRepository.getSubdirFilePath(widgetFilesDir, widget.getId(), null).resolve(imageToDelete));
                    for (ImageSize imageSize : ImageSize.values()) {
                        fileRepository.delete(fileRepository.getSubdirFilePath(widgetFilesDir, widget.getId(), null)
                                .resolve(imageSize.name() + "-" + imageToDelete));
                    }
                })
        );

        pageContent.setId(page.getId());
        page.setPageContent(pageContent);
        pageRepository.save(page);

        computeEditable(pageContent, roles);

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
            List<String> allImagesInFolder = getFiles(fileRepository.getDirFromId(widgetFilesDir, widget.getId()), null);
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

        Page page = pageRepository.findById(pageId).orElseThrow();
        page.getPageContent().getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename);
            }
        });

        pageRepository.save(page);

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
    public byte[] loadFile(String widgetId, String filename, ImageSize targetSize) {
        FileSystemResource file = loadFile(widgetFilesDir, widgetId, filename, targetSize);
        return fileRepository.readBytes(file.getFile().toPath());
    }

    /**
     * Computes if the page can be edited with the given roles.
     *
     * @param pageContent The page to check.
     * @param roles       The available roles.
     */
    private void computeEditable(PageContent pageContent, Set<String> roles) {
        var adminRoleRequired = pageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getRestrictions)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .anyMatch(Roles.ROLE_ADMIN::equals);

        var userRoleRequired = pageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getRestrictions)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .anyMatch(Roles.ROLE_USER::equals);

        boolean adminRequirementMet = !adminRoleRequired || roles.contains(Roles.ROLE_ADMIN);
        boolean userRequirementMet = !userRoleRequired || roles.contains(Roles.ROLE_USER);

        pageContent.setEditable(adminRequirementMet && userRequirementMet);
    }

}
