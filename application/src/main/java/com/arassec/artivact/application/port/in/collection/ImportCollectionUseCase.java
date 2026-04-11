package com.arassec.artivact.application.port.in.collection;

import java.nio.file.Path;

/**
 * Use case for import collection operations.
 */
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

    /**
     * Imports previously created collection exports to the application by saving the exported ZIP file.
     * Authenticates via API token.
     *
     * @param file     The exported ZIP file.
     * @param apiToken The API token for authentication and authorization.
     */
    void importCollectionForDistribution(Path file, String apiToken);

}
