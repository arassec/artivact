package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.ArtivactImporter;
import com.arassec.artivact.domain.exchange.ExchangeProcessor;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for handling item imports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportService extends BaseFileService implements ExchangeProcessor {

    /**
     * The service for account handling.
     */
    private final AccountService accountService;

    /**
     * Importer for Artivact's data.
     */
    private final ArtivactImporter artivactImporter;

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * The service's progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Executor service for background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Imports previously exported content data to the application by reading from the exported ZIP file.
     *
     * @param file     The exported ZIP file.
     * @param apiToken The API token of the user to use for item import.
     */
    public void importContent(MultipartFile file, String apiToken) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "importContent");

        if (StringUtils.hasText(apiToken)) {
            Optional<Account> accountOptional = accountService.loadByApiToken(apiToken);
            if (accountOptional.isEmpty()) {
                return;
            }
            Account account = accountOptional.get();
            if (Boolean.TRUE.equals(!account.getUser()) && Boolean.TRUE.equals(!account.getAdmin())) {
                return;
            }
        }

        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("import.zip");
        Path importFileZip = projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.TEMP_DIR)
                .resolve(originalFilename)
                .toAbsolutePath();

        try {
            file.transferTo(importFileZip);
        } catch (IOException e) {
            throw new ArtivactException("Could not save uploaded ZIP file!", e);
        }

        executorService.submit(() -> {
            try {
                artivactImporter.importContent(importFileZip);
                fileRepository.delete(importFileZip);
                progressMonitor = null;
            } catch (Exception e) {
                log.error("Error during content import!", e);
                progressMonitor.updateProgress("importContentFailed", e);
            }
        });
    }

}
