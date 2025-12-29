package com.arassec.artivact.application.port.in.project;

import java.nio.file.Path;

/**
 * Use case for use project dirs operations.
 */
public interface UseProjectDirsUseCase {

    Path getProjectRoot();

    /**
     * Returns the temp dir.
     *
     * @return The result.
     */
    Path getTempDir();

    /**
     * Returns the exports dir.
     *
     * @return The result.
     */
    Path getExportsDir();

    /**
     * Returns the items dir.
     *
     * @return The result.
     */
    Path getItemsDir();

    /**
     * Returns the widgets dir.
     *
     * @return The result.
     */
    Path getWidgetsDir();

    /**
     * Returns the search index dir.
     *
     * @return The result.
     */
    Path getSearchIndexDir();

    /**
     * Returns the images dir.
     *
     * @param itemId The item ID.
     *
     * @return The result.
     */
    Path getImagesDir(String itemId);

    /**
     * Returns the models dir.
     *
     * @param itemId The item ID.
     *
     * @return The result.
     */
    Path getModelsDir(String itemId);

}
