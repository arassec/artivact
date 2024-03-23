package com.arassec.artivact.backend.service.creator.util;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Watches a directory for filesystem changes, i.e. added files, that can then further be processed.
 */
@Slf4j
@Component
public class DirectoryWatcher {

    /**
     * Indicates that the watcher should stop watching for new files.
     */
    private final AtomicBoolean stopWatching = new AtomicBoolean(false);

    /**
     * Thread executor to watch a directory in a separate thread.
     */
    private ExecutorService executorService;

    /**
     * Counts detected files.
     */
    private int detectedFiles;

    /**
     * Contains the expected number of new files.
     */
    private int numExpectedFiles;

    /**
     * Starts watching the provided directory for new files and calls the given callback if a new file is detected.
     *
     * @param targetDir        The target directory to watch.
     * @param numExpectedFiles The number of expected files to detect.
     * @param callback         Callback to call when a new file is detected.
     */
    public void startWatching(Path targetDir, int numExpectedFiles, DirectoryWatcherCallback callback) {
        this.numExpectedFiles = numExpectedFiles;
        detectedFiles = 0;
        stopWatching.set(false);

        executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();

                targetDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                while (!stopWatching.get()) {
                    WatchKey watchKey = watchService.poll(100, TimeUnit.MILLISECONDS);
                    if (watchKey != null) {
                        for (WatchEvent<?> event : watchKey.pollEvents()) {
                            detectedFiles++;
                            callback.processNewFile(targetDir.resolve((Path) event.context()));
                        }
                        watchKey.reset();
                    }
                }

                watchService.close();
                log.debug("Watchservice closed!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactException("Could not watch target dir for new images!", e);
            } catch (Exception e) {
                throw new ArtivactException("Could not watch target dir for new images!", e);
            }
        });

        // Give the watch service some time to get started!
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during directory watcher setup!");
        }
    }

    /**
     * Finishes watching the directory by waiting for the expected number of files (or a timeout)
     * and stopping the watching thread.
     *
     * @param timeout A timeout in milliseconds until the watcher will wait for the expected number of files ot be added.
     */
    public void finishWatching(int timeout) {
        try {
            int waitTime = 0;
            while ((detectedFiles < numExpectedFiles) && waitTime < timeout) {
                TimeUnit.MILLISECONDS.sleep(50);
                waitTime += 50;
            }

            stopWatching.set(true);

            executorService.shutdown();
            while (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                log.debug("Awaiting executor service termination!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Interrupted during shutdown!", e);
        }

    }

}
