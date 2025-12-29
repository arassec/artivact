package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for load collection export cover picture operations.
 */
public interface LoadCollectionExportCoverPictureUseCase {

    /**
     * Loads a collection export's cover picture.
     *
     * @param id The ID of the collection export.
     * @return The image as byte array.
     */
    byte[] loadCoverPicture(String id);

}
