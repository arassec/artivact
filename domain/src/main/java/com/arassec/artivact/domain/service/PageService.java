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
import org.springframework.util.StringUtils;
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
     * @param pageIdOrAlias The page's ID or alias.
     */
    public void deletePage(String pageIdOrAlias) {
        Optional<Page> pageOptional = pageRepository.deleteById(pageIdOrAlias);
        if (pageOptional.isPresent()) {
            pageOptional.get().getPageContent().getWidgets().forEach(
                    widget -> deleteDirAndEmptyParents(fileRepository.getDirFromId(widgetFilesDir, widget.getId()))
            );
            pageRepository.deleteById(pageIdOrAlias);
        }
    }

    /**
     * Updates a page's alias and restrictions.
     *
     * @param pageId       The page's ID.
     * @param pageAlias    The page's alias to use.
     * @param restrictions The new restrictions to apply.
     */
    public void updatePageAliasAndRestrictions(String pageId, String pageAlias, Set<String> restrictions) {
        pageRepository.findById(pageId).ifPresent(page -> {
            page.setAlias(pageAlias);
            page.getPageContent().setRestrictions(restrictions);
            pageRepository.save(page);
        });
    }

    /**
     * Loads the index page if available.
     *
     * @return The index {@link Page}.
     */
    @TranslateResult
    @RestrictResult
    public Page loadIndexPage(Set<String> roles) {
        Page indexPage = pageRepository.findIndexPage();
        computeEditable(indexPage.getPageContent(), roles);
        return indexPage;
    }

    /**
     * Loads the content of the given page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    public PageContent loadPageContent(String pageIdOrAlias, Set<String> roles) {
        if (!StringUtils.hasText(pageIdOrAlias)) {
            throw new ArtivactException("Page id or alias is missing!");
        }
        Optional<Page> pageOptional = pageRepository.findByIdOrAlias(pageIdOrAlias);
        Page page = pageOptional.orElseThrow();
        computeEditable(page.getPageContent(), roles);
        return page.getPageContent();
    }

    /**
     * Loads the content of the given page and applies translations and restrictions.
     *
     * @param pageIdOrAlias The page's ID or the page's alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    @TranslateResult
    @RestrictResult
    public PageContent loadTranslatedRestrictedPageContent(String pageIdOrAlias, Set<String> roles) {
        return loadPageContent(pageIdOrAlias, roles);
    }

    /**
     * Saves a page's content.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @param pageContent   The content to save.
     * @return The updated page content.
     */
    @GenerateIds
    @TranslateResult
    @RestrictResult
    public PageContent savePageContent(String pageIdOrAlias, Set<String> roles, PageContent pageContent) {
        Optional<Page> pageOptional = pageRepository.findByIdOrAlias(pageIdOrAlias);

        Page page;
        if (pageOptional.isPresent()) {
            page = pageOptional.get();
        } else {
            // Importing content from a previous export:
            page = new Page();
            page.setId(pageIdOrAlias);
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
     * Saves a file for a given widget in the filesystem.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param file     The file to save.
     * @return The name of the saved file.
     */
    public String saveFile(String pageId, String widgetId, MultipartFile file) {

        String filename = saveFile(widgetFilesDir, widgetId, file);

        Page page = pageRepository.findByIdOrAlias(pageId).orElseThrow();
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
