package com.arassec.artivact.application.service.maintenance;

import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.maintenance.CleanupProjectFilesUseCase;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Service for cleanup project files operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupProjectFilesService implements CleanupProjectFilesUseCase {

    /**
     * Loads menus.
     */
    private final LoadMenuUseCase loadMenuUseCase;

    /**
     * Saves menus.
     */
    private final SaveMenuUseCase saveMenuUseCase;

    /**
     * Loads pages.
     */
    private final LoadPageContentUseCase loadPageContentUseCase;

    /**
     * Saves pages.
     */
    private final SavePageContentUseCase savePageContentUseCase;

    /**
     * Loads properties configuration.
     */
    private final LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    /**
     * Saves properties configuration.
     */
    private final SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;

    /**
     * Loads tags configuration.
     */
    private final LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    /**
     * Saves tags configuration.
     */
    private final SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    /**
     * Loads the appearance configuration.
     */
    private final LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    /**
     * Saves the appearance configuration.
     */
    private final SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

    /**
     * Use case for item searching.
     */
    private final SearchItemsUseCase searchItemsUseCase;

    /**
     * Use case for saving items.
     */
    private final SaveItemUseCase saveItemUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        log.info("Cleaning up project files.");
        loadMenuUseCase.loadTranslatedRestrictedMenus().forEach(this::processMenu);
        savePropertiesConfigurationUseCase.savePropertiesConfiguration(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration());
        saveTagsConfigurationUseCase.saveTagsConfiguration(loadTagsConfigurationUseCase.loadTagsConfiguration());
        saveAppearanceConfigurationUseCase.saveAppearanceConfiguration(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration());
        searchItemsUseCase.search("*", Integer.MAX_VALUE).forEach(saveItemUseCase::save);
        log.info("Done cleaning up project files.");
    }

    /**
     * Loads and saves menu, page and widgets.
     */
    private void processMenu(Menu menu) {
        saveMenuUseCase.saveMenu(menu);
        if (StringUtils.hasText(menu.getTargetPageId())) {
            PageContent pageContent = loadPageContentUseCase.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_USER, Roles.ROLE_ADMIN));
            savePageContentUseCase.savePageContent(pageContent.getId(), Set.of(Roles.ROLE_USER, Roles.ROLE_ADMIN), pageContent);
        }
        menu.getMenuEntries().forEach(this::processMenu);
    }

}
