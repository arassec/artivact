package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.project.CleanupExportFilesUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
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

    private final FileRepository fileRepository;

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Removes a previously exported tags configuration file.
     */
    @Override
    public void cleanupTagsConfigurationExport() {
        fileRepository.delete(useProjectDirsUseCase.getProjectRoot()
                .resolve(DirectoryDefinitions.EXPORT_DIR)
                .resolve(ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON));
    }

    /**
     * Removes a previously exported properties configuration file.
     */
    @Override
    public void cleanupPropertiesConfigurationExport() {
        fileRepository.delete(useProjectDirsUseCase.getProjectRoot()
                .resolve(DirectoryDefinitions.EXPORT_DIR)
                .resolve(ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON));
    }

}
