package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.ArtivactImporter;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Service for handling item imports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportService extends BaseFileService {

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
     * Imports a previously exported content export.
     *
     * @param file     The content export file to import.
     * @param apiToken An API token for server-to-server communication.
     */
    public void importContent(MultipartFile file, String apiToken) {
        if (!StringUtils.hasText(apiToken)) {
            throw new IllegalArgumentException("API token cannot be empty!");
        }
        Account account = accountService.loadByApiToken(apiToken).orElseThrow();
        if (!(Boolean.TRUE.equals(account.getUser()) || Boolean.TRUE.equals(!account.getAdmin()))) {
            throw new IllegalArgumentException("Item import not allowed!");
        }
        importContent(file);
    }

    /**
     * Imports a previously exported content export.
     *
     * @param file The content export file to import.
     */
    public void importContent(MultipartFile file) {
        Path importFileZip = saveFile(projectDataProvider.getProjectRoot(), file);
        artivactImporter.importContent(importFileZip);
        fileRepository.delete(importFileZip);
    }

}
