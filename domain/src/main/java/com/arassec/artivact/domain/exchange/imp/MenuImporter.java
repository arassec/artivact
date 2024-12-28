package com.arassec.artivact.domain.exchange.imp;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ImportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.MenuService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.MENU_EXCHANGE_FILE_SUFFIX;

/**
 * Importer for {@link Menu}s.
 */
@Component
@RequiredArgsConstructor
public class MenuImporter {

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

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
            if (!menu.getMenuEntries().isEmpty()) {
                menu.getMenuEntries().forEach(menuEntry -> {
                    menuEntry.setParentId(menuId);
                    importMenu(importContext, menuEntry.getId(), false);
                });
            } else if (StringUtils.hasText(menu.getTargetPageId())) {
                pageImporter.importPage(importContext, menu.getTargetPageId());
            }

            if (saveMenu) {
                Optional<Path> coverPictureOptional = fileRepository.list(importContext.getImportDir()).stream()
                        .filter(file -> file.getFileName().toString().startsWith("cover-picture"))
                        .findFirst();

                if (coverPictureOptional.isPresent()) {
                    Path coverPictureTargetDir = fileRepository.getSubdirFilePath(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.MENUS_DIR), menuId, null);
                    fileRepository.createDirIfRequired(coverPictureTargetDir);
                    fileRepository.copy(coverPictureOptional.get(), coverPictureTargetDir.resolve(coverPictureOptional.get().getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                }

                // When importing exported sub-menus the parent is set to null and the sub-menu is imported as regular menu.
                menu.setParentId(null);

                menuService.saveMenu(menu);
            }
        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import menu!", e);
        }
    }

}
