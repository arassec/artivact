package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.BaseRestrictedObject;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.*;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.MenuRepository;
import com.arassec.artivact.core.repository.PageIdAndAlias;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for page handling.
 */
@Slf4j
@Service
@Transactional
public class PageService extends BaseFileService {

    /**
     * Regular expression for matching UUIDs.
     */
    private static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * Repository for menu configurations.
     */
    private final MenuRepository menuRepository;

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
     * Path to the widget data directory.
     */
    private final Path widgetFilesDir;

    /**
     * Creates a new instance.
     *
     * @param pageRepository      Repository for pages.
     * @param menuRepository      Repository for menus.
     * @param fileRepository      The application's {@link FileRepository}.
     * @param objectMapper        The object mapper.
     * @param projectDataProvider Provider for project data.
     */
    public PageService(PageRepository pageRepository,
                       MenuRepository menuRepository,
                       FileRepository fileRepository,
                       ObjectMapper objectMapper,
                       ProjectDataProvider projectDataProvider) {
        this.pageRepository = pageRepository;
        this.menuRepository = menuRepository;
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
     * @param pageId    The page's ID.
     * @param pageAlias The page's alias to use.
     */
    public void updatePageAlias(String pageId, String pageAlias) {
        pageRepository.findById(pageId).ifPresent(page -> {
            page.setAlias(pageAlias);
            pageRepository.save(page);
        });
    }

    /**
     * Loads the index page if available.
     *
     * @return The index {@link Page}.
     */
    public Optional<PageIdAndAlias> loadIndexPageIdAndAlias() {
        return pageRepository.findIndexPageId();
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
        Page page = pageOptional.orElseThrow(() -> new ArtivactException("Page not found for ID or alias: " + pageIdOrAlias));
        computeEditable(page.getPageContent(), roles);

        page.getPageContent().setRestrictions(findMenuRestrictions(page.getId()));

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

        // Workaround to circumvent concurrent page modifications. When saving a page using its ID, there might have
        // been an alias for the page been configured using the menu configuration. To prevent this alias from being
        // overwritten with a null value, we store the persisted alias here und set it later on.
        String persistedPageAlias = null;

        Page page;
        if (pageOptional.isPresent()) {
            page = pageOptional.get();

            // If the page is saved using its ID, we have to keep a persisted page alias. The alias might have been
            // added during page editing by a menu configuration:
            if (UUID_REGEX.matcher(pageIdOrAlias).matches()) {
                persistedPageAlias = page.getAlias();
            }

        } else {
            // Importing content from a previous export:
            page = new Page();
            page.setId(pageIdOrAlias);
            page.setVersion(0);
            page.setPageContent(pageContent);
        }

        Optional<Menu> menuOptional = findMenu(page.getId());
        if (menuOptional.isEmpty()) {
            throw new ArtivactException("No menu found for page: " + pageIdOrAlias);
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

        // Keep the alias if available:
        if (StringUtils.hasText(persistedPageAlias)) {
            page.setAlias(persistedPageAlias);
        }

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
    public synchronized String saveFile(String pageId, String widgetId, MultipartFile file) {

        String filename = saveFile(widgetFilesDir, widgetId, file);

        Page page = pageRepository.findByIdOrAlias(pageId).orElseThrow();
        page.getPageContent().getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename, FileProcessingOperation.ADD);
            }
        });

        pageRepository.save(page);

        return filename;
    }

    /**
     * Deletes a file for a given widget from the filesystem.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param filename The file to delete.
     */
    public PageContent deleteFile(String pageId, String widgetId, String filename) {

        Path filePath = getSimpleFilePath(widgetFilesDir, widgetId, filename);
        fileRepository.delete(filePath);

        // If the widget stores images, we remove scaled versions of them, too:
        for (ImageSize imageSize : ImageSize.values()) {
            Path scaledImage = getSimpleFilePath(widgetFilesDir, widgetId, imageSize.name() + "-" + filename);
            fileRepository.delete(scaledImage);
        }

        Page page = pageRepository.findByIdOrAlias(pageId).orElseThrow();
        page.getPageContent().getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename, FileProcessingOperation.REMOVE);
            }
        });

        pageRepository.save(page);

        return page.getPageContent();
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
     * Returns a list of a widget's images that are not referenced by the widget itself but only existing in the
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

    /**
     * Determines menu restrictions for the menu associated with the given page.
     *
     * @param pageId The page's ID.
     * @return Set of restrictions from the corresponding menu.
     */
    private Set<String> findMenuRestrictions(String pageId) {
        for (Menu menu : menuRepository.load().getMenus()) {
            if (pageId.equals(menu.getTargetPageId())) {
                return menu.getRestrictions();
            }
            for (Menu menuEntry : menu.getMenuEntries()) {
                if (pageId.equals(menuEntry.getTargetPageId())) {
                    if (!menuEntry.getRestrictions().isEmpty()) {
                        return menuEntry.getRestrictions();
                    } else {
                        return menu.getRestrictions();
                    }
                }
            }
        }
        return Set.of();
    }

    /**
     * Finds the menu related to the given page.
     *
     * @param pageId The page's ID.
     * @return The menu whose target page is the given one.
     */
    private Optional<Menu> findMenu(String pageId) {
        for (Menu menu : menuRepository.load().getMenus()) {
            if (pageId.equals(menu.getTargetPageId())) {
                return Optional.of(menu);
            }
            for (Menu menuEntry : menu.getMenuEntries()) {
                if (pageId.equals(menuEntry.getTargetPageId())) {
                    return Optional.of(menuEntry);
                }
            }
        }
        return Optional.empty();
    }

}
