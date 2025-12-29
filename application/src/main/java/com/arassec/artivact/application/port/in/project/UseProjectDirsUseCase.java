package com.arassec.artivact.application.port.in.project;

import java.nio.file.Path;

/**
 * Use case for use project dirs operations.
 */
public interface UseProjectDirsUseCase {

    Path getProjectRoot();

    Path getTempDir();

    Path getExportsDir();

    Path getItemsDir();

    Path getWidgetsDir();

    Path getSearchIndexDir();

    Path getImagesDir(String itemId);

    Path getModelsDir(String itemId);

}
