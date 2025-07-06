package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.page.*;
import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.BaseRestrictedObject;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for page handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PageService
        implements LoadPageContentUseCase,
        SavePageContentUseCase,
        UpdatePageAliasUseCase,
        ManagePageMediaUseCase,
        CreatePageUseCase,
        DeletePageUseCase {

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

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Creates a new page.
     *
     * @param restrictions Set of restrictions that should apply to the new page.
     * @return The newly created page.
     */
    @Override
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
    @Override
    public void deletePage(String pageIdOrAlias) {
        Optional<Page> pageOptional = pageRepository.deleteById(pageIdOrAlias);
        if (pageOptional.isPresent()) {
            pageOptional.get().getPageContent().getWidgets().forEach(widget
                    -> fileRepository.deleteDirAndEmptyParents(
                            fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widget.getId()))
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
    @Override
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
    @Override
    public Optional<PageRepository.PageIdAndAlias> loadIndexPageIdAndAlias() {
        return pageRepository.findIndexPageId();
    }

    /**
     * Loads the content of the given page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    @Override
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
    @Override
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
    @Override
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

        widgetIdsToDelete.forEach(widgetId
                -> fileRepository.deleteDirAndEmptyParents(fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widgetId)));

        pageContent.getWidgets().forEach(widget ->
                getDanglingImages(widget).forEach(imageToDelete -> {
                    fileRepository.delete(fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), null).resolve(imageToDelete));
                    for (ImageSize imageSize : ImageSize.values()) {
                        fileRepository.delete(fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), null)
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
    @Override
    public synchronized String saveFile(String pageId, String widgetId, MultipartFile file) {

        String filename = saveFile(useProjectDirsUseCase.getWidgetsDir(), widgetId, file);

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
    @Override
    public PageContent deleteFile(String pageId, String widgetId, String filename) {

        Path filePath = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, filename);
        fileRepository.delete(filePath);

        // If the widget stores images, we remove scaled versions of them, too:
        for (ImageSize imageSize : ImageSize.values()) {
            Path scaledImage = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, imageSize.name() + "-" + filename);
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
    @Override
    public byte[] loadFile(String widgetId, String filename, ImageSize targetSize) {
        FileSystemResource file = loadFile(useProjectDirsUseCase.getWidgetsDir(), widgetId, filename, targetSize);
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
            List<String> allImagesInFolder = fileRepository.listNamesWithoutScaledImages(
                    fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widget.getId()));
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

    /**
     * Saves the given file under subdirectories of the root directory based on the given ID.
     * <p>
     * E.g., with root "/path/to/dir" and ID "123ABC" the resulting location of the file will be under
     * "/path/to/dir/123/ABC/123ABC".
     *
     * @param root The root path to store the file in.
     * @param id   An ID which is used to generate subdirectories to place the file in.
     * @param file The file to save.
     * @return The name of the saved file.
     */
    private String saveFile(Path root, String id, MultipartFile file) {
        String filename = file.getOriginalFilename();

        Path filePath = fileRepository.getSubdirFilePath(root, id, filename);

        try {
            fileRepository.createDirIfRequired(filePath.getParent());
            fileRepository.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save file!", e);
        }

        return filename;
    }

    /**
     * Loads a file from a subdirectory of the root path based on the given ID.
     *
     * @param root       The root path to get the file from.
     * @param id         The ID to use to determine subdirectories.
     * @param filename   The name of the file to load.
     * @param targetSize The desired target size of an image, if that's to be loaded.
     * @return A {@link FileSystemResource} to the file.
     */
    private FileSystemResource loadFile(Path root, String id, String filename, ImageSize targetSize) {
        if (targetSize != null) {
            return fileRepository.loadImage(root, id, filename, targetSize, ".");
        }
        return new FileSystemResource(fileRepository.getSubdirFilePath(root, id, filename));
    }

}
