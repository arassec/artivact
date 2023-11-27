package com.arassec.artivact.backend.service.creator.adapter;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
     * The file util.
     */
    //protected final FileUtil fileUtil;

    /**
     * Message source for I18N.
     */
    //protected final MessageSource messageSource;

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
     * Copies all images from the provided {@link ImageSet} to the provided destination.
     *
     * @param projectRoot     The project's root directory.
     * @param imageSet        The image set containing the images to copy.
     * @param destination     The destination path to copy the images to.
     * @param progressMonitor A progress monitor which is updated during copying.
     */
    protected void copyImages(Path projectRoot, ImageSet imageSet, Path destination, ProgressMonitor progressMonitor) {
        var index = new AtomicInteger(1);
        imageSet.getImages().forEach(image -> {
            progressMonitor.setProgress("Copying image " + index.getAndIncrement() + "/" + imageSet.getImages().size());
            Path source = projectRoot.resolve(image.getPath());
            String[] parts = image.getPath().split("/");
            Path target = destination.resolve(parts[parts.length - 1]);
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactException("Could not copy artivact images for model creation!", e);
            }
        });
    }

}
