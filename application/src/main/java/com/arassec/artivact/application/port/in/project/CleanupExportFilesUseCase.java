package com.arassec.artivact.application.port.in.project;

/**
 * Use case for cleanup export files operations.
 */
public interface CleanupExportFilesUseCase {

    /**
     * Removes a previously exported properties configuration file.
     */
    void cleanupPropertiesConfigurationExport();

    /**
     * Removes a previously exported tags configuration file.
     */
    void cleanupTagsConfigurationExport();

}
