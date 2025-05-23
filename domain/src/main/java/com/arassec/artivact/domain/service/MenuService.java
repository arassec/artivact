package com.arassec.artivact.domain.service;


import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.MenuConfiguration;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.MenuRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.exchange.ArtivactExporter;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for menu handling.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MenuService extends BaseFileService {

    /**
     * Repository to configurations.
     */
    private final MenuRepository menuRepository;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Artivact Exporter.
     */
    private final ArtivactExporter artivactExporter;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * Loads translated menus.
     *
     * @return The currently configured, translated application menus.
     */
    @RestrictResult
    @TranslateResult
    public List<Menu> loadTranslatedRestrictedMenus() {
        return menuRepository.load().getMenus();
    }

    /**
     * Saves the given menus.
     *
     * @param menus The menus to save.
     * @return The updated, translated menus after saving.
     */
    @GenerateIds
    @RestrictResult
    @TranslateResult
    public List<Menu> saveMenus(List<Menu> menus) {
        MenuConfiguration menuConfiguration = menuRepository.load();

        menuConfiguration.setMenus(menus);

        menuRepository.save(menuConfiguration);

        menuConfiguration.getMenus().forEach(menu -> {
            if (StringUtils.hasText(menu.getTargetPageId())) {
                pageService.updatePageAlias(menu.getTargetPageId(), menu.getTargetPageAlias());
            }
            menu.getMenuEntries().forEach(menuEntry ->
                    pageService.updatePageAlias(menuEntry.getTargetPageId(), menu.getTargetPageAlias()));
        });

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Saves a single menu.
     *
     * @param menu The menu to save.
     * @return All application menus, translated, and including the new menu.
     */
    @GenerateIds
    @RestrictResult
    @TranslateResult
    public List<Menu> saveMenu(Menu menu) {
        if (menu == null) {
            return loadTranslatedRestrictedMenus();
        }

        if (!StringUtils.hasText(menu.getValue())) {
            throw new ArtivactException("Menu title required!");
        }

        menu.getMenuEntries().forEach(menuEntry -> {
            if (!StringUtils.hasText(menuEntry.getValue())) {
                throw new ArtivactException("Sub-Menu title required!");
            }
        });

        MenuConfiguration menuConfiguration = menuRepository.load();

        Optional<Menu> existingMenuOptional = menuConfiguration.getMenus().stream()
                .filter(existingMenu -> existingMenu.getId().equals(menu.getId()))
                .findFirst();

        if (existingMenuOptional.isPresent()) {
            Menu existingMenu = existingMenuOptional.get();
            existingMenu.setValue(menu.getValue());
            existingMenu.setTranslations(menu.getTranslations());
            existingMenu.setRestrictions(menu.getRestrictions());
            existingMenu.setTargetPageId(menu.getTargetPageId());
            existingMenu.setTargetPageAlias(menu.getTargetPageAlias());
            existingMenu.setHidden(menu.isHidden());
            existingMenu.setMenuEntries(menu.getMenuEntries());
        } else {
            menuConfiguration.getMenus().add(menu);
        }

        if (StringUtils.hasText(menu.getTargetPageId())) {
            pageService.updatePageAlias(menu.getTargetPageId(), menu.getTargetPageAlias());
        }
        menu.getMenuEntries().forEach(menuEntry -> {
            if (menuEntry.getRestrictions().isEmpty() && !menu.getRestrictions().isEmpty()) {
                pageService.updatePageAlias(menuEntry.getTargetPageId(), menuEntry.getTargetPageAlias());
            } else {
                pageService.updatePageAlias(menuEntry.getTargetPageId(), menuEntry.getTargetPageAlias());
            }
        });

        // We add pages for sub-menu entries automatically, except when they point to external pages:
        menuConfiguration.getMenus().forEach(existingMenu ->
                existingMenu.getMenuEntries().forEach(existingMenuEntry -> {
                    if (!StringUtils.hasText(existingMenuEntry.getTargetPageId()) && !StringUtils.hasText(existingMenuEntry.getExternal())) {
                        Page page = pageService.createPage(existingMenuEntry.getRestrictions());
                        page.setAlias(existingMenuEntry.getTargetPageAlias());
                        existingMenuEntry.setTargetPageId(page.getId());
                    }
                }));

        menuRepository.save(menuConfiguration);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Deletes a menu.
     *
     * @param menuId The ID of the menu to delete.
     * @return All remaining application menus, translated.
     */
    @RestrictResult
    @TranslateResult
    public List<Menu> deleteMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to delete menu!");
            return loadTranslatedRestrictedMenus();
        }

        MenuConfiguration menuConfiguration = menuRepository.load();

        List<String> pagesToDelete = new LinkedList<>();

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                // Main menu to delete:
                pagesToDelete.addAll(menu.getMenuEntries().stream()
                        .map(Menu::getTargetPageId)
                        .toList());
                pagesToDelete.add(menu.getTargetPageId());
            } else {
                // Search menu entries for possible delete candidate:
                pagesToDelete.addAll(menu.getMenuEntries().stream()
                        .filter(menuEntry -> menuEntry.getId().equals(menuId))
                        .map(Menu::getTargetPageId)
                        .toList());
            }
        });

        pagesToDelete.stream()
                .filter(Objects::nonNull)
                .forEach(pageService::deletePage);

        // Delete sub-menus:
        menuConfiguration.getMenus()
                .forEach(menu -> menu.setMenuEntries(menu.getMenuEntries().stream()
                        .filter(menuEntry -> !menuEntry.getId().equals(menuId))
                        .toList()));

        menuConfiguration.setMenus(menuConfiguration.getMenus().stream()
                .filter(existingMenu -> !existingMenu.getId().equals(menuId))
                .toList());

        menuRepository.save(menuConfiguration);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Adds an empty page to the given menu.
     *
     * @param menuId The ID of the menu to add a new page to.
     * @return All application menus, translated.
     */
    @RestrictResult
    @TranslateResult
    public List<Menu> addPageToMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to add page to!");
            return loadTranslatedRestrictedMenus();
        }

        MenuConfiguration menuConfiguration = menuRepository.load();

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                Page page = pageService.createPage(menu.getRestrictions());
                page.setAlias(menu.getTargetPageAlias());
                menu.setTargetPageId(page.getId());
            }
        });

        menuRepository.save(menuConfiguration);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Returns the menu with the given ID, be it main- or sub-menu.
     *
     * @param menuId The ID of the menu to find.
     * @return The menu with the given ID.
     */
    @TranslateResult
    public Menu findMenu(String menuId) {
        List<Menu> flattenedMenus = loadTranslatedRestrictedMenus();
        flattenedMenus.addAll(flattenedMenus.stream()
                .flatMap(existingMenu -> existingMenu.getMenuEntries().stream())
                .toList());
        return flattenedMenus.stream()
                .filter(Objects::nonNull)
                .filter(existingMenu -> existingMenu.getId().equals(menuId))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Exports a menu without items.
     *
     * @param menuId The ID of the menu to export.
     * @return Path to the export file.
     */
    public Path exportMenu(String menuId) {
        Menu menu = findMenu(menuId);
        return artivactExporter.exportMenu(menu);
    }

}
