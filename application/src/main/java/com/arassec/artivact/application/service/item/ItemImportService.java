package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.item.ImportItemUseCase;
import com.arassec.artivact.application.port.in.item.SaveItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.*;

/**
 * Service for item imports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemImportService implements ImportItemUseCase {

    /**
     * Use case for save item.
     */
    private final SaveItemUseCase saveItemUseCase;

    /**
     * Use case for load account.
     */
    private final LoadAccountUseCase loadAccountUseCase;

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
    public void importItem(Path contentExport, String apiToken) {
        if (!StringUtils.hasText(apiToken)) {
            throw new ArtivactException("API token cannot be empty!");
        }

        Account account = loadAccountUseCase.loadByApiToken(apiToken).orElseThrow();
        if (!Boolean.TRUE.equals(account.getUser()) && !Boolean.TRUE.equals(account.getAdmin())) {
            throw new ArtivactException("Item import not allowed!");
        }

        importItem(contentExport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importItem(Path contentExport) {
        ImportContext importContext = ImportContext.builder()
                .importDir(useProjectDirsUseCase.getTempDir()
                        .resolve(contentExport.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                .build();

        fileRepository.unpack(contentExport, importContext.getImportDir());

        try {
            ExchangeMainData exchangeMainData =
                    jsonMapper.readValue(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON).toFile(), ExchangeMainData.class);

            if (!ContentSource.ITEM.equals(exchangeMainData.getContentSource())) {
                throw new ArtivactException("Invalid content source for item import: " + exchangeMainData.getContentSource());
            }

            exchangeMainData.getSourceIds().forEach(itemId -> importItem(importContext, itemId));

            importPropertiesConfigurationUseCase.importPropertiesConfiguration(importContext);
            importTagsConfigurationUseCase.importTagsConfiguration(importContext);

            fileRepository.delete(importContext.getImportDir());

        } catch (Exception e) {
            throw new ArtivactException("Could not import item!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importItem(ImportContext importContext, String itemId) {
        Path itemSourceDir = fileRepository.getDirFromId(importContext.getImportDir().resolve(DirectoryDefinitions.ITEMS_DIR), itemId);
        Path itemJsonFile = itemSourceDir.resolve(ITEM_EXCHANGE_FILENAME_JSON);
        if (!fileRepository.exists(itemJsonFile)) {
            log.warn("No item json file found for item import with id '{}'. Skipping item import.", itemId);
            return;
        }

        String itemJson = fileRepository.read(itemJsonFile);
        Item item = jsonMapper.readValue(itemJson, Item.class);

        Path itemTargetDir = fileRepository.getDirFromId(useProjectDirsUseCase.getItemsDir(), itemId);
        fileRepository.createDirIfRequired(itemTargetDir);
        fileRepository.copy(itemSourceDir.resolve(DirectoryDefinitions.IMAGES_DIR), itemTargetDir.resolve(DirectoryDefinitions.IMAGES_DIR));
        fileRepository.copy(itemSourceDir.resolve(DirectoryDefinitions.MODELS_DIR), itemTargetDir.resolve(DirectoryDefinitions.MODELS_DIR));

        saveItemUseCase.save(item);
    }

}
