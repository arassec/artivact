package com.arassec.artivact.application.port.in.project;

import java.nio.file.Path;

public interface UseProjectDirsUseCase {

    Path getProjectRoot();

    Path getItemsDir();

    Path getWidgetsDir();

    Path getImagesDir(String itemId);

    Path getModelsDir(String itemId);

}
