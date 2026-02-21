package com.arassec.artivact.application.service.batch.processor;

import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.page.SavePageContentUseCase;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.batch.BatchProcessingParameters;
import com.arassec.artivact.domain.model.batch.BatchProcessingTask;
import com.arassec.artivact.domain.model.batch.BatchProcessor;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Batch processor that cleans up project files.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupProjectFilesBatchProcessor implements BatchProcessor {

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
     * Processes menus and pages and their widgets.
     */
    @Override
    public void initialize() {
        loadMenuUseCase.loadTranslatedRestrictedMenus().forEach(this::processMenu);
        savePropertiesConfigurationUseCase.savePropertiesConfiguration(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration());
        saveTagsConfigurationUseCase.saveTagsConfiguration(loadTagsConfigurationUseCase.loadTagsConfiguration());
        saveAppearanceConfigurationUseCase.saveAppearanceConfiguration(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration());
    }

    /**
     * Processes items by marking them as "to be saved".
     * When saved, obsolete files are deleted, references to missing files are removed from the item.
     */
    @Override
    public boolean process(BatchProcessingParameters params, Item item) {
        // If this task is executed, just save the item.
        return BatchProcessingTask.CLEANUP_PROJECT_FILES.equals(params.getTask());
    }

    /**
     * Loads and saves menu, page and widgets.
     */
    private void processMenu(Menu menu) {
        saveMenuUseCase.saveMenu(menu);
        if (StringUtils.hasText(menu.getTargetPageId())) {
            PageContent pageContent = loadPageContentUseCase.loadPageContent(menu.getTargetPageId(), Set.of(Roles.ROLE_ADMIN));
            savePageContentUseCase.savePageContent(pageContent.getId(), Set.of(Roles.ADMIN), pageContent);
        }
        menu.getMenuEntries().forEach(this::processMenu);
    }

}
