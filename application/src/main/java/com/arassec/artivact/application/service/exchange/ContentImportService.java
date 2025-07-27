package com.arassec.artivact.application.service.exchange;

import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.exchange.ImportContentUseCase;
import com.arassec.artivact.application.port.in.item.ImportItemUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.exchange.ContentSource;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;

import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON;
import static com.arassec.artivact.domain.model.misc.ExchangeDefinitions.ZIP_FILE_SUFFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentImportService implements ImportContentUseCase {

    private final ImportItemUseCase importItemUseCase;

    @Getter
    private final ObjectMapper objectMapper;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

    @Getter
    private final UseProjectDirsUseCase useProjectDirsUseCase;
    private final ImportMenuUseCase importMenuUseCase;
    private final ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;
    private final ImportTagsConfigurationUseCase importTagsConfigurationUseCase;
    private final LoadAccountUseCase loadAccountUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void importContent(Path contentExport) {
        ImportContext importContext = ImportContext.builder()
                .importDir(useProjectDirsUseCase.getProjectRoot()
                        .resolve(DirectoryDefinitions.TEMP_DIR)
                        .resolve(contentExport.getFileName().toString().replace(ZIP_FILE_SUFFIX, "")))
                .build();

        fileRepository.unpack(contentExport, importContext.getImportDir());

        try {
            ExchangeMainData exchangeMainData = readExchangeMainDataJson(importContext.getImportDir().resolve(CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON));

            if (ContentSource.MENU.equals(exchangeMainData.getContentSource())) {
                importMenuUseCase.importMenu(importContext, exchangeMainData.getSourceId(), true);
            } else if (ContentSource.ITEM.equals(exchangeMainData.getContentSource())) {
                importItemUseCase.importItem(importContext, exchangeMainData.getSourceId());
            } else {
                throw new ArtivactException("Unknown content source: " + exchangeMainData.getContentSource());
            }

            importPropertiesConfigurationUseCase.importPropertiesConfiguration(importContext);
            importTagsConfigurationUseCase.importTagsConfiguration(importContext);

            fileRepository.delete(importContext.getImportDir());

        } catch (Exception e) {
            throw new ArtivactException("Could not import data!", e);
        }
    }

    /**
     * Imports a previously exported content export.
     *
     * @param contentExport The content export file to import.
     * @param apiToken      An API token for server-to-server communication.
     */
    public void importContent(Path contentExport, String apiToken) {
        if (!StringUtils.hasText(apiToken)) {
            throw new IllegalArgumentException("API token cannot be empty!");
        }
        Account account = loadAccountUseCase.loadByApiToken(apiToken).orElseThrow();
        if (!(Boolean.TRUE.equals(account.getUser()) || !account.getAdmin())) {
            throw new IllegalArgumentException("Item import not allowed!");
        }
        importContent(contentExport);
    }

    private ExchangeMainData readExchangeMainDataJson(Path file) {
        try {
            return getObjectMapper().readValue(file.toFile(), ExchangeMainData.class);
        } catch (IOException e) {
            throw new ArtivactException("Could not read ExchangeMainData JSON file " + file, e);
        }
    }

}
