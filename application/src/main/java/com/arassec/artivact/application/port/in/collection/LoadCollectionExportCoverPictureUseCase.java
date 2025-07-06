package com.arassec.artivact.application.port.in.collection;

public interface LoadCollectionExportCoverPictureUseCase {

    /**
     * Loads a collection export's cover picture.
     *
     * @param id The ID of the collection export.
     * @return The image as byte array.
     */
    byte[] loadCoverPicture(String id);

}
