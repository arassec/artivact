package com.arassec.artivact.application.creator.util;

import com.arassec.artivact.application.service.item.util.DirectoryWatcher;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DirectoryWatcher}.
 */
public class DirectoryWatcherTest {

    /**
     * The watched directory.
     */
    private final Path watchedDirectory = Path.of("target/directory-watcher-test");

    /**
     * Tests watching a directory for newly created files.
     *
     * @throws IOException in case of setup errors.
     */
    @Test
    void testDirectoryWatcher() throws IOException {

        FileUtils.deleteDirectory(watchedDirectory.toFile());
        Files.createDirectories(watchedDirectory);

        DirectoryWatcher directoryWatcher = new DirectoryWatcher();

        AtomicBoolean fileFound = new AtomicBoolean(false);
        directoryWatcher.startWatching(watchedDirectory, 1, (Path newFile) -> {
            if (newFile.getFileName().toString().equals("test-file.txt")) {
                fileFound.set(true);
            }
        });

        Path file = watchedDirectory.resolve("test-file.txt");
        Files.createFile(file);

        directoryWatcher.finishWatching(500);

        assertThat(fileFound.get()).isTrue();
    }

}
