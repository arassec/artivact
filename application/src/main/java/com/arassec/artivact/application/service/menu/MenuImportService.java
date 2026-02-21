package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.ImportPageUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

/**
 * Service for menu import.
 */
@Service
@RequiredArgsConstructor
public class MenuImportService implements ImportMenuUseCase {

    /**
     * The JSON mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * Repository for file.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for import page.
     */
    private final ImportPageUseCase importPageUseCase;

    /**
     * Use case for save menu.
     */
    private final SaveMenuUseCase saveMenuUseCase;

    /**
     * Use case for import properties configuration.
     */
    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    /**
     * Use case for import tags configuration.
     */
    private final ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void importMenu(Path contentExport) {
        ImportContext importContext = ImportContext.builder()
                .importDir(useProjectDirsUseCase.getTempDir()
                        .resolve(contentExport.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                .build();

        fileRepository.unpack(contentExport, importContext.getImportDir());

        try {
            ExchangeMainData exchangeMainData =
                    jsonMapper.readValue(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile(), ExchangeMainData.class);

            if (!ContentSource.MENU.equals(exchangeMainData.getContentSource()) &&
                    !(ContentSource.COLLECTION.equals(exchangeMainData.getContentSource()) && !exchangeMainData.getSourceIds().isEmpty())) {
                throw new ArtivactException("Invalid content source for menu import: " + exchangeMainData.getContentSource());
            }

            exchangeMainData.getSourceIds().forEach(menuId -> importMenu(importContext, menuId, true));

            importPropertiesConfigurationUseCase.importPropertiesConfiguration(importContext);
            importTagsConfigurationUseCase.importTagsConfiguration(importContext);

            fileRepository.delete(importContext.getImportDir());

        } catch (Exception e) {
            throw new ArtivactException("Could not import data!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importMenu(ImportContext importContext, String menuId, boolean saveMenu) {
        Path menuJson = fileRepository.getDirFromId(importContext.getImportDir().resolve(DirectoryDefinitions.MENUS_DIR), menuId)
                .resolve(MENU_EXCHANGE_FILENAME_JSON);
        Menu menu = jsonMapper.readValue(fileRepository.read(menuJson), Menu.class);

        if (saveMenu) {
            // When directly importing former submenus as menus, the parent is set to "null" and the (former) submenu
            // is thus imported as a regular menu.
            menu.setParentId(null);
            saveMenuUseCase.saveMenu(menu);
        }

        if (!menu.getMenuEntries().isEmpty()) {
            menu.getMenuEntries().forEach(menuEntry -> {
                menuEntry.setParentId(menuId);
                importMenu(importContext, menuEntry.getId(), false);
            });
        }

        if (StringUtils.hasText(menu.getTargetPageId())) {
            importPageUseCase.importPage(importContext, menu.getTargetPageId(), menu.getTargetPageAlias());
        }
    }

}
