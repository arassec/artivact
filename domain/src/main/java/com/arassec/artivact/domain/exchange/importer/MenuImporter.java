package com.arassec.artivact.domain.exchange.importer;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.service.MenuService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.MENU_EXCHANGE_FILE_SUFFIX;

/**
 * Importer for {@link Menu}s.
 */
@Component
@RequiredArgsConstructor
public class MenuImporter {

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Service for item handling.
     */
    private final PageImporter pageImporter;

    /**
     * Service for item handling.
     */
    private final MenuService menuService;

    /**
     * Imports a menu.
     *
     * @param importContext The import context.
     * @param menuId        The menu's ID.
     */
    public void importMenu(ImportContext importContext, String menuId, boolean saveMenu) {
        Path menuJson = importContext.getImportDir().resolve(menuId + MENU_EXCHANGE_FILE_SUFFIX);
        try {
            Menu menu = objectMapper.readValue(fileRepository.read(menuJson), Menu.class);

            if (saveMenu) {
                // When directly importing former submenus as menus, the parent is set to "null" and the (former) submenu
                // is thus imported as a regular menu.
                menu.setParentId(null);
                menuService.saveMenu(menu);
            }

            if (!menu.getMenuEntries().isEmpty()) {
                menu.getMenuEntries().forEach(menuEntry -> {
                    menuEntry.setParentId(menuId);
                    importMenu(importContext, menuEntry.getId(), false);
                });
            }

            if (StringUtils.hasText(menu.getTargetPageId())) {
                pageImporter.importPage(importContext, menu.getTargetPageId(), menu.getTargetPageAlias());
            }

        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import menu!", e);
        }
    }

}
