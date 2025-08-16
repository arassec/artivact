package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.ImportPageUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

@Service
@RequiredArgsConstructor
public class MenuImportService implements ImportMenuUseCase {

    private final ObjectMapper objectMapper;

    private final FileRepository fileRepository;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final ImportPageUseCase importPageUseCase;

    private final SaveMenuUseCase saveMenuUseCase;

    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

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
            ExchangeMainData exchangeMainData = readExchangeMainDataJson(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON));
            importMenu(importContext, exchangeMainData.getSourceId(), true);
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
        Path menuJson = importContext.getImportDir().resolve(menuId + MENU_EXCHANGE_FILE_SUFFIX);
        try {
            Menu menu = objectMapper.readValue(fileRepository.read(menuJson), Menu.class);

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

        } catch (JsonProcessingException e) {
            throw new ArtivactException("Could not import menu!", e);
        }
    }

    private ExchangeMainData readExchangeMainDataJson(Path file) {
        try {
            return objectMapper.readValue(file.toFile(), ExchangeMainData.class);
        } catch (IOException e) {
            throw new ArtivactException("Could not read ExchangeMainData JSON file " + file, e);
        }
    }

}
