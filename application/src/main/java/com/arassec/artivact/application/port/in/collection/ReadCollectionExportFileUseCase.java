package com.arassec.artivact.application.port.in.collection;

import java.io.InputStream;

public interface ReadCollectionExportFileUseCase {

    /**
     * Returns a collection export's file as stream.
     *
     * @param id The collection export's ID.
     * @return An {@link InputStream} containing the export file.
     */
    InputStream readExportFile(String id);

}
