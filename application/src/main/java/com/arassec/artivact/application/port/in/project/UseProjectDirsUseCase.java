package com.arassec.artivact.application.port.in.project;

import java.nio.file.Path;

/**
 * Use case for use project dirs operations.
 */
public interface UseProjectDirsUseCase {

    /**
     * Gets the project's root directory.
     *
     * @return The project's root directory.
     */
    Path getProjectRoot();

    /**
     * Gets the temporary directory.
     *
     * @return The temporary directory.
     */
    Path getTempDir();

    /**
     * Gets the exports directory.
     *
     * @return The exports directory.
     */
    Path getExportsDir();

    /**
     * Gets the items directory.
     *
     * @return The items directory.
     */
    Path getItemsDir();

    /**
     * Gets the widgets directory.
     *
     * @return The widgets directory.
     */
    Path getWidgetsDir();

    /**
     * Gets the search index directory.
     *
     * @return The search index directory.
     */
    Path getSearchIndexDir();

    /**
     * Gets the images directory for the specified item.
     *
     * @param itemId The item ID.
     * @return The images directory for the specified item.
     */
    Path getImagesDir(String itemId);

    /**
     * Gets the models directory for the specified item.
     *
     * @param itemId The item ID.
     * @return The models directory for the specified item.
     */
    Path getModelsDir(String itemId);

}
