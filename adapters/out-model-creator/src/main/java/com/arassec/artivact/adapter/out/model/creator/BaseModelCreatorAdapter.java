package com.arassec.artivact.adapter.out.model.creator;

import com.arassec.artivact.application.port.out.adapter.ModelCreatorAdapter;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.adapter.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for model-creator adapter implementations.
 */
@Slf4j
public abstract class BaseModelCreatorAdapter extends BasePeripheralAdapter implements ModelCreatorAdapter {

    /**
     * The export subdirectory where model creators should export their results in.
     */
    protected static final String EXPORT_DIR = "export/";

    /**
     * TODO: Move to helper use case?
     * Executes a command on the system's command line and waits for the results.
     *
     * @param cmdLine The command to execute.
     */
    protected void execute(CommandLine cmdLine) {
        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = DaemonExecutor.builder().get();
        executor.setExitValue(1);

        log.debug("Executing command: {}", cmdLine);

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

        log.debug("Executed command finished (success={}).", executionSuccessful);
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
                // TODO: Use FileRepository!
                Files.copy(image, destination.resolve(image.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactException("Could not copy artivact images for model creation!", e);
            }
        });
    }

}
