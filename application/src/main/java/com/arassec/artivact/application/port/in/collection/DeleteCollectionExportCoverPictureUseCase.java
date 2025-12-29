package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for delete collection export cover picture operations.
 */
public interface DeleteCollectionExportCoverPictureUseCase {

    /**
     * Deletes the cover picture of a collection export.
     *
     * @param id The collection export's ID.
     */
    void deleteCoverPicture(String id);

}
