package com.arassec.artivact.application.port.in.collection;

import java.nio.file.Path;

public interface ImportCollectionUseCase {

    /**
     * Imports previously created collection exports to the application by reading from the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    void importCollection(Path file);

    /**
     * Imports previously created collection exports to the application by saving the exported ZIP file.
     *
     * @param file The exported ZIP file.
     */
    void importCollectionForDistribution(Path file);

}
