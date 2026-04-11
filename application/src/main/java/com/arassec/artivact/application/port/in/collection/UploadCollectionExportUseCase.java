package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for uploading collection exports to remote instances.
 */
public interface UploadCollectionExportUseCase {

    /**
     * Uploads a collection export to the configured remote Artivact instance.
     *
     * @param collectionExportId The collection export's ID.
     */
    void uploadCollectionExportToRemoteInstance(String collectionExportId);

}
