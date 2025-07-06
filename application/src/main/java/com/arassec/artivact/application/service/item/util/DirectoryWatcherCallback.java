package com.arassec.artivact.application.service.item.util;

import java.nio.file.Path;

/**
 * Callback for processing logic. Will be used by the {@link DirectoryWatcher} in case new files are detected in the
 * watched directory.
 */
public interface DirectoryWatcherCallback {

    /**
     * Callback that is triggered by the {@link DirectoryWatcher} if a new file is detected.
     *
     * @param file the new file.
     */
    void processNewFile(Path file);

}
