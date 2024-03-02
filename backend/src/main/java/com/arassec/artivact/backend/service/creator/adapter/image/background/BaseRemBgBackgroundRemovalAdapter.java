package com.arassec.artivact.backend.service.creator.adapter.image.background;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Base class for rembg-based background removal adapters.
 */
@Slf4j
public abstract class BaseRemBgBackgroundRemovalAdapter extends BaseBackgroundRemovalAdapter {

    /**
     * An executor service to remove backgrounds multithreaded.
     */
    protected ExecutorService executorService;

    /**
     * The result list containing paths to the processed image files without background.
     */
    protected final Collection<Path> result = Collections.synchronizedCollection(new LinkedList<>());

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, BackgroundRemovalInitParams initParams) {
        super.initialize(progressMonitor, initParams);
        executorService = Executors.newFixedThreadPool(4);
        result.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackground(Path filePath) {
        executorService.submit(() -> {
            log.debug("Removing background from image: {}", filePath.getFileName());
            result.add(removeBackgroundFromImage(initParams.getAdapterConfiguration(), filePath, initParams.getTargetDir()));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBackgrounds(List<Path> filePaths) {
        progressMonitor.updateLabelKey("rembgInProgress");
        filePaths.forEach(filePath -> {
            log.debug("Removing background from image: {}", filePath);
            removeBackground(filePath);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Path>> teardown() {
        executorService.shutdown();

        boolean shutdown = false;
        while (!shutdown) {
            try {
                shutdown = executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactException("Interrupted during image background removal!", e);
            }
        }

        // Cleanup AFTER the threads have finished!
        super.teardown();

        return Optional.of(new LinkedList<>(result));
    }

    /**
     * Removes the background from the supplied input file and stores the resulting image in the provided output directory.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @param inputFile            The original image with background.
     * @param targetDir            The directory to put the resulting image in.
     * @return The path to the newly created image file.
     */
    protected abstract Path removeBackgroundFromImage(AdapterConfiguration adapterConfiguration, Path inputFile, Path targetDir);

}
