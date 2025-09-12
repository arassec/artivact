package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.page.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.BaseRestrictedObject;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.*;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for page handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManagePageService
        implements LoadPageContentUseCase,
        SavePageContentUseCase,
        UpdatePageAliasUseCase,
        ManagePageMediaUseCase,
        CreatePageUseCase,
        DeletePageUseCase,
        ResetWipPageContentUseCase,
        PublishWipPageContentUseCase {

    /**
     * Regular expression for matching UUIDs.
     */
    private static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * The directory for 'work-in-progress' widget files.
     */
    private static final String WIDGET_WIP_DIR = "wip";

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * Repository for menu configurations.
     */
    private final MenuRepository menuRepository;

    /**
     * Repository for configurations.
     */
    private final ConfigurationRepository configurationRepository;

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
     * Use case for project directory handling.
     */
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
    public Optional<PageIdAndAlias> loadIndexPageIdAndAlias() {
        AppearanceConfiguration appearanceConfiguration = configurationRepository
                .findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class).orElse(new AppearanceConfiguration());

        String indexPageId = appearanceConfiguration.getIndexPageId();

        if (!StringUtils.hasText(indexPageId)) {
            return Optional.empty();
        }

        Optional<Page> optionalPage = pageRepository.findById(indexPageId);

        return optionalPage.map(page -> new PageIdAndAlias(indexPageId, page.getAlias()));

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
     * Loads the 'work-in-progress' content of the given page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @return The {@link PageContent} of the page.
     */
    @TranslateResult
    @RestrictResult
    @Override
    public PageContent loadTranslatedRestrictedWipPageContent(String pageIdOrAlias, Set<String> roles) {
        if (!StringUtils.hasText(pageIdOrAlias)) {
            throw new ArtivactException("Page id or alias is missing!");
        }

        Optional<Page> pageOptional = pageRepository.findByIdOrAlias(pageIdOrAlias);
        Page page = pageOptional.orElseThrow(() -> new ArtivactException("Page not found for ID or alias: " + pageIdOrAlias));

        // Check if the wip-folder needs initialization:
        AtomicBoolean wipInitialized = new AtomicBoolean(false);
        page.getPageContent().getWidgets().forEach(widget -> {
            Path widgetDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), null);
            Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), WIDGET_WIP_DIR);
            if (fileRepository.exists(widgetDir) && !fileRepository.exists(widgetWipDir)) {
                wipInitialized.set(true);
                fileRepository.createDirIfRequired(widgetWipDir);
                fileRepository.list(widgetDir).stream()
                        .filter(widgetFile -> !widgetFile.getFileName().toString().equals(WIDGET_WIP_DIR))
                        .forEach(widgetFile -> fileRepository.copy(widgetFile, widgetWipDir.resolve(widgetFile.getFileName())));
            }
        });

        PageContent pageContent = page.getWipPageContent();
        if (wipInitialized.get()) {
            pageContent = page.getPageContent();
            page.setWipPageContent(pageContent);
            pageRepository.save(page);
        }

        computeEditable(pageContent, roles);

        pageContent.setRestrictions(findMenuRestrictions(page.getId()));

        return pageContent;
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
            try {
                // Create a deep copy of the original page content:
                page.setWipPageContent(objectMapper.readValue(objectMapper.writeValueAsString(pageContent), PageContent.class));
            } catch (JsonProcessingException e) {
                throw new ArtivactException("Could not create WIP-PageContent!", e);
            }
        }

        Optional<Menu> menuOptional = findMenu(page.getId());
        if (menuOptional.isEmpty()) {
            throw new ArtivactException("No menu found for page: " + pageIdOrAlias);
        }

        PageContent existingPageContent = page.getPageContent();
        PageContent existingWipPageContent = page.getWipPageContent();

        computeEditable(existingWipPageContent, roles);

        if (!existingWipPageContent.isEditable()) {
            throw new ArtivactException("Page can not be edited by the current user!");
        }

        // Keep all widgets that are in productive use:
        List<String> widgetIdsToRetain = existingPageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .collect(Collectors.toList());
        // Also keep all widgets currently in 'work-in-progress':
        widgetIdsToRetain.addAll(pageContent.getWidgets().stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        // Those widgets are completely removed:
        List<String> widgetIdsToDelete = existingWipPageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .collect(Collectors.toList());
        widgetIdsToDelete.removeAll(widgetIdsToRetain);

        // Those widgets are only removed from 'work-in-progress', but retain on the published page:
        List<String> widgetIdsToCleanWip = existingWipPageContent.getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .collect(Collectors.toList());
        widgetIdsToCleanWip.removeAll(pageContent.getWidgets().stream()
                .map(BaseRestrictedObject::getId)
                .toList());

        widgetIdsToDelete.forEach(widgetId
                -> fileRepository.deleteDirAndEmptyParents(fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widgetId)));

        widgetIdsToCleanWip.forEach(widgetId -> {
            Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, WIDGET_WIP_DIR);
            if (fileRepository.exists(widgetWipDir)) {
                fileRepository.delete(widgetWipDir);
                // Create the empty 'wip' directory to prevent widget initialization on page load!
                fileRepository.createDirIfRequired(widgetWipDir);
            }
        });

        pageContent.getWidgets().forEach(this::cleanDanglingImages);

        pageContent.setId(page.getId());
        page.setWipPageContent(pageContent);

        // Keep the alias if available:
        if (StringUtils.hasText(persistedPageAlias)) {
            page.setAlias(persistedPageAlias);
        }

        page.setVersion(page.getVersion() + 1);

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
        page.getWipPageContent().getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename, FileProcessingOperation.ADD);
            }
        });

        page.getWipPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .filter(widget -> widget.getId().equals(widgetId))
                .findFirst()
                .ifPresent(this::cleanDanglingImages);

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

        Path filePath = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, WIDGET_WIP_DIR).resolve(filename);
        fileRepository.delete(filePath);

        // If the widget stores images, we remove scaled versions of them, too:
        for (ImageSize imageSize : ImageSize.values()) {
            Path scaledImage = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, WIDGET_WIP_DIR).resolve(imageSize.name() + "-" + filename);
            fileRepository.delete(scaledImage);
        }

        Page page = pageRepository.findByIdOrAlias(pageId).orElseThrow();
        page.getWipPageContent().getWidgets().forEach(widget -> {
            if (widget.getId().equals(widgetId) && widget instanceof FileProcessingWidget fileProcessingWidget) {
                fileProcessingWidget.processFile(filename, FileProcessingOperation.REMOVE);
            }
        });

        pageRepository.save(page);

        return page.getWipPageContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadFile(String widgetId, String filename, ImageSize targetSize, boolean wip) {
        FileSystemResource file = loadFile(useProjectDirsUseCase.getWidgetsDir(), widgetId, filename, targetSize, wip);
        return fileRepository.readBytes(file.getFile().toPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageContent resetWipPageContent(String pageIdOrAlias) {
        Page page = pageRepository.findByIdOrAlias(pageIdOrAlias).orElseThrow();

        // Keep all widgets that are in productive use:
        List<String> widgetsToRetain = page.getPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .toList();

        List<String> widgetIdsToDelete = page.getWipPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .filter(id -> !widgetsToRetain.contains(id))
                .toList();

        widgetIdsToDelete.forEach(widgetId
                -> fileRepository.deleteDirAndEmptyParents(fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widgetId)));

        widgetsToRetain.forEach(widgetId -> {
            Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, WIDGET_WIP_DIR);
            if (fileRepository.exists(widgetWipDir)) {
                fileRepository.delete(widgetWipDir);
            }
        });

        page.getPageContent().getWidgets().forEach(widget -> {
            if (widget instanceof FileProcessingWidget fileProcessingWidget) {
                Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), WIDGET_WIP_DIR);
                fileRepository.createDirIfRequired(widgetWipDir);
                fileProcessingWidget.usedFiles()
                        .forEach(widgetFile -> fileRepository.copy(widgetWipDir.getParent().resolve(widgetFile), widgetWipDir.resolve(widgetFile)));
            }
        });

        page.setWipPageContent(page.getPageContent());

        pageRepository.save(page);

        return page.getPageContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageContent publishWipPageContent(String pageIdOrAlias) {
        Page page = pageRepository.findByIdOrAlias(pageIdOrAlias).orElseThrow();

        // Keep all widgets that are in 'work-in-progress' use:
        List<String> widgetsToRetain = page.getWipPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .toList();

        // Delete all widgets only used on the published page:
        List<String> widgetIdsToDelete = page.getPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .map(BaseRestrictedObject::getId)
                .filter(id -> !widgetsToRetain.contains(id))
                .toList();

        widgetIdsToDelete.forEach(widgetId
                -> fileRepository.deleteDirAndEmptyParents(fileRepository.getDirFromId(useProjectDirsUseCase.getWidgetsDir(), widgetId)));

        widgetsToRetain.forEach(widgetId -> {
            Path widgetDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, null);
            Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, WIDGET_WIP_DIR);
            fileRepository.list(widgetDir).forEach(widgetFile -> {
                if (!WIDGET_WIP_DIR.equals(widgetFile.getFileName().toString())) {
                    fileRepository.delete(widgetFile);
                }
            });
            fileRepository.list(widgetWipDir)
                    .forEach(widgetWipFile -> fileRepository.copy(widgetWipFile, widgetDir.resolve(widgetWipFile.getFileName()), StandardCopyOption.REPLACE_EXISTING));
        });

        page.setPageContent(page.getWipPageContent());

        pageRepository.save(page);

        return page.getPageContent();
    }

    /**
     * Removes all unused images from the widget's directory.
     *
     * @param widget The widget to clean dangling images of.
     */
    private void cleanDanglingImages(Widget widget) {
        getDanglingImages(widget).forEach(imageToDelete -> {
            fileRepository.delete(fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), WIDGET_WIP_DIR).resolve(imageToDelete));
            for (ImageSize imageSize : ImageSize.values()) {
                fileRepository.delete(fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), WIDGET_WIP_DIR)
                        .resolve(imageSize.name() + "-" + imageToDelete));
            }
        });
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
                    fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widget.getId(), WIDGET_WIP_DIR));
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

        if (!StringUtils.hasText(filename)) {
            throw new ArtivactException("No filename specified for uploaded file!");
        }

        Path filePath = fileRepository.getSubdirFilePath(root, id, WIDGET_WIP_DIR);

        try {
            fileRepository.createDirIfRequired(filePath);
            fileRepository.copy(file.getInputStream(), filePath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
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
     * @param wip        Specifies whether the 'work-in-progress' version or the productive one should be loaded.
     * @return A {@link FileSystemResource} to the file.
     */
    private FileSystemResource loadFile(Path root, String id, String filename, ImageSize targetSize, boolean wip) {
        String imagesSubDir = ".";
        if (wip) {
            imagesSubDir = "wip";
        }
        if (targetSize != null) {
            return fileRepository.loadImage(root, id, filename, targetSize, imagesSubDir);
        }
        return new FileSystemResource(fileRepository.getSubdirFilePath(root, id, WIDGET_WIP_DIR).resolve(filename));
    }
}
