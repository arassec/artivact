package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.project.CleanupExportFilesUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base for services that need file handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectFileService implements CleanupExportFilesUseCase {

    /**
     * Repository for file.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Removes a previously exported tags configuration file.
     */
    @Override
    public void cleanupTagsConfigurationExport() {
        fileRepository.delete(useProjectDirsUseCase.getExportsDir()
                .resolve(ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON));
    }

    /**
     * Removes a previously exported properties configuration file.
     */
    @Override
    public void cleanupPropertiesConfigurationExport() {
        fileRepository.delete(useProjectDirsUseCase.getExportsDir()
                .resolve(ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON));
    }

}
