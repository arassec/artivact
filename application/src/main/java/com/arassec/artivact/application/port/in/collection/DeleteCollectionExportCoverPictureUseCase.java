package com.arassec.artivact.application.port.in.collection;

public interface DeleteCollectionExportCoverPictureUseCase {

    /**
     * Deletes the cover picture of a collection export.
     *
     * @param id The collection export's ID.
     */
    void deleteCoverPicture(String id);

}
