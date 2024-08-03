package com.arassec.artivact.domain.creator.adapter;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.model.item.CreationImageSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;

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


    /**
     * Executes a command on the system's command line and waits for the results.
     *
     * @param cmdLine The command to execute.
     * @return {@code true} if the command executed successfully, {@code false} otherwise.
     */
    protected boolean execute(CommandLine cmdLine) {
        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = DaemonExecutor.builder().get();
        executor.setExitValue(1);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during 'execute' operation!", e);
        }

        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted during 'execute' operation!", e);
            Thread.currentThread().interrupt();
        }

        boolean executionSuccessful = resultHandler.getExitValue() == 0 || resultHandler.getException() == null;

        if (!executionSuccessful) {
            log.error("Exception during command execution!", resultHandler.getException());
        }

        return executionSuccessful;
    }

}
