package com.arassec.artivact.application.service.menu;


import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.PersistEntityAsJson;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.menu.*;
import com.arassec.artivact.application.port.in.page.CreatePageUseCase;
import com.arassec.artivact.application.port.in.page.DeletePageUseCase;
import com.arassec.artivact.application.port.in.page.UpdatePageAliasUseCase;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Service for menu handling.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ManageMenuService
        implements SaveMenuUseCase,
        LoadMenuUseCase,
        DeleteMenuUseCase,
        AddPageToMenuUseCase,
        RelocateMenuUseCase {

    /**
     * Repository to configurations.
     */
    private final MenuRepository menuRepository;

    /**
     * Use case for update page alias.
     */
    private final UpdatePageAliasUseCase updatePageAliasUseCase;

    /**
     * Use case for create page.
     */
    private final CreatePageUseCase createPageUseCase;

    /**
     * Use case for delete page.
     */
    private final DeletePageUseCase deletePageUseCase;

    /**
     * {@inheritDoc}
     */
    @RestrictResult
    @TranslateResult
    @Override
    public List<Menu> loadTranslatedRestrictedMenus() {
        return loadMenusSorted();
    }

    /**
     * {@inheritDoc}
     */
    @GenerateIds
    @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class)
    @RestrictResult
    @TranslateResult
    @Override
    public List<Menu> saveMenus(List<Menu> menus) {
        saveMenuList(menus);

        menus.forEach(menu -> {
            if (StringUtils.hasText(menu.getTargetPageId())) {
                updatePageAliasUseCase.updatePageAlias(menu.getTargetPageId(), menu.getTargetPageAlias());
            }
            menu.getMenuEntries().forEach(menuEntry ->
                    updatePageAliasUseCase.updatePageAlias(menuEntry.getTargetPageId(), menuEntry.getTargetPageAlias()));
        });

        return loadTranslatedRestrictedMenus();
    }

    /**
     * {@inheritDoc}
     */
    @GenerateIds
    @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class)
    @RestrictResult
    @TranslateResult
    @Override
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

        List<Menu> menus = loadMenusSorted();

        Optional<Menu> existingMenuOptional = menus.stream()
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
            existingMenu.setExternal(menu.getExternal());
        } else {
            menus.add(menu);
        }

        if (StringUtils.hasText(menu.getTargetPageId())) {
            updatePageAliasUseCase.updatePageAlias(menu.getTargetPageId(), menu.getTargetPageAlias());
        }
        menu.getMenuEntries().forEach(menuEntry ->
                updatePageAliasUseCase.updatePageAlias(menuEntry.getTargetPageId(), menuEntry.getTargetPageAlias())
        );

        // We add pages for sub-menu entries automatically, except when they point to external pages:
        menus.forEach(existingMenu ->
                existingMenu.getMenuEntries().forEach(existingMenuEntry -> {
                    if (!StringUtils.hasText(existingMenuEntry.getTargetPageId()) && !StringUtils.hasText(existingMenuEntry.getExternal())) {
                        Page page = createPageUseCase.createPage(existingMenuEntry.getRestrictions());
                        page.setAlias(existingMenuEntry.getTargetPageAlias());
                        existingMenuEntry.setTargetPageId(page.getId());
                    }
                }));

        saveMenuList(menus);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Deletes a menu.
     *
     * @param menuId The ID of the menu to delete.
     * @return All remaining application menus, translated.
     */
    @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class, delete = true)
    @RestrictResult
    @TranslateResult
    @Override
    public List<Menu> deleteMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to delete menu!");
            return loadTranslatedRestrictedMenus();
        }

        List<Menu> menus = loadMenusSorted();

        List<String> pagesToDelete = new LinkedList<>();

        menus.forEach(menu -> {
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
                .forEach(deletePageUseCase::deletePage);

        // Delete sub-menus:
        menus.forEach(menu -> menu.setMenuEntries(menu.getMenuEntries().stream()
                        .filter(menuEntry -> !menuEntry.getId().equals(menuId))
                        .toList()));

        menus.removeIf(existingMenu -> existingMenu.getId().equals(menuId));

        saveMenuList(menus);

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
    @Override
    public List<Menu> addPageToMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to add page to!");
            return loadTranslatedRestrictedMenus();
        }

        List<Menu> menus = loadMenusSorted();

        menus.forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                Page page = createPageUseCase.createPage(menu.getRestrictions());
                page.setAlias(menu.getTargetPageAlias());
                menu.setTargetPageId(page.getId());
            }
        });

        saveMenuList(menus);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Returns the menu with the given ID, be it main- or sub-menu.
     *
     * @param menuId The ID of the menu to find.
     * @return The menu with the given ID.
     */
    @TranslateResult
    @Override
    public Menu loadMenu(String menuId) {
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
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void relocateMenu(String menuId, String newParentMenuId) {

        List<Menu> menus = loadMenusSorted();

        List<Menu> flattenedMenus = new LinkedList<>();
        flattenedMenus.addAll(menus);
        flattenedMenus.addAll(flattenedMenus.stream()
                .flatMap(existingMenu -> existingMenu.getMenuEntries().stream())
                .toList());

        Menu sourceMenu = flattenedMenus.stream()
                .filter(menu -> menu.getId().equals(menuId))
                .findFirst()
                .orElseThrow();

        if (StringUtils.hasText(sourceMenu.getParentId())) {
            Menu oldParentMenu = flattenedMenus.stream()
                    .filter(menu -> menu.getId().equals(sourceMenu.getParentId()))
                    .findFirst()
                    .orElseThrow();
            oldParentMenu.getMenuEntries().remove(sourceMenu);
        }

        if ("main".equals(newParentMenuId)) {
            sourceMenu.setParentId(null);
            menus.add(sourceMenu);
        } else {
            Menu targetMenu = flattenedMenus.stream()
                    .filter(menu -> menu.getId().equals(newParentMenuId))
                    .findFirst()
                    .orElseThrow();
            sourceMenu.setParentId(targetMenu.getId());
            targetMenu.getMenuEntries().add(sourceMenu);
            // Remove from main menu if necessary:
            menus.remove(sourceMenu);
        }

        saveMenuList(menus);
    }

    /**
     * Loads all menus sorted by their index. The result list must be modifiable
     * as callers (e.g. saveMenu, relocateMenu) add or remove entries.
     *
     * @return Sorted modifiable list of menus.
     */
    @SuppressWarnings("java:S6204") // The result list of menus needs to be modifiable!
    private List<Menu> loadMenusSorted() {
        List<Menu> menus = menuRepository.load();
        menus.sort(Comparator.comparingInt(Menu::getIndex));
        return menus;
    }

    /**
     * Saves all menus, calculating and setting the index for each menu.
     *
     * @param menus The list of menus to save.
     */
    private void saveMenuList(List<Menu> menus) {
        Set<String> currentMenuIds = new HashSet<>();
        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);
            menu.setIndex(i);
            currentMenuIds.add(menu.getId());
            menuRepository.save(menu);
        }

        // Delete menus that are no longer in the list:
        menuRepository.load().stream()
                .map(Menu::getId)
                .filter(id -> !currentMenuIds.contains(id))
                .forEach(menuRepository::delete);
    }

}
