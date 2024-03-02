package com.arassec.artivact.backend.service.creator.adapter;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.CreationImageSet;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for adapter implementations.
 *
 * @param <I> Type of the optional initialization parameter.
 * @param <T> Result type of the finalize method call.
 */
@Slf4j
public abstract class BaseAdapter<I extends AdapterInitParams, T> implements Adapter<I, T> {

    /**
     * The progress monitor to give feedback about the adapter status to the user.
     */
    protected ProgressMonitor progressMonitor;

    /**
     * Object with optional parameters for specific adapter implementations.
     */
    protected I initParams;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(AdapterImplementation adapterImplementation) {
        return getSupportedImplementation().equals(adapterImplementation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ProgressMonitor progressMonitor, I initParams) {
        this.progressMonitor = progressMonitor;
        this.initParams = initParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<T> teardown() {
        this.initParams = null;
        return Optional.empty();
    }

    /**
     * Copies all images from the provided {@link CreationImageSet} to the provided destination.
     *
     * @param images          The images to copy.
     * @param destination     The destination path to copy the images to.
     * @param progressMonitor A progress monitor which is updated during copying.
     */
    protected void copyImages(List<Path> images, Path destination, ProgressMonitor progressMonitor) {
        progressMonitor.updateLabelKey("copyImages");
        var index = new AtomicInteger(1);
        images.forEach(image -> {
            progressMonitor.updateProgress(index.getAndIncrement(), images.size());
            try {
                Files.copy(image, destination.resolve(image.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactException("Could not copy artivact images for model creation!", e);
            }
        });
    }

}
