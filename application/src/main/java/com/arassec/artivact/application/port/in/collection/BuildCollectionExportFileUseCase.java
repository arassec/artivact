package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for build collection export file operations.
 */
public interface BuildCollectionExportFileUseCase {

    /**
     * (Re-)Builds a collection export's package file.
     *
     * @param id The collection export's ID.
     */
    void buildExportFile(String id);

}
