package com.arassec.artivact.application.port.in.collection;

import java.io.InputStream;

public interface SaveCollectionExportCoverPictureUseCase {

    /**
     * Saves the provided image as cover picture of the collection export.
     *
     * @param id               The collection export's ID.
     * @param originalFilename The image's original name.
     * @param inputStream      The input stream providing the image data.
     */
    void saveCoverPicture(String id, String originalFilename, InputStream inputStream);

}
