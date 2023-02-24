package com.arassec.artivact.creator.standalone.core.adapter;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class BaseAdapter {

    protected void executeCommandLine(CommandLine cmdLine) {
        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DaemonExecutor();
        executor.setExitValue(1);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during camera operation!", e);
        }

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted during camera operation!", e);
            Thread.currentThread().interrupt();
        }
    }

    protected void copyImages(CreatorArtivact creatorArtivact, ArtivactImageSet imageSet, Path destination, ProgressMonitor progressMonitor) {
        var index = new AtomicInteger(1);
        imageSet.getImages().forEach(artivactImage -> {
            progressMonitor.setProgress("Copying image " + index.getAndIncrement() + "/" + imageSet.getImages().size());
            Path source = creatorArtivact.getProjectRoot().resolve(artivactImage.getPath());
            String[] parts = artivactImage.getPath().split("/");
            Path target = destination.resolve(parts[parts.length - 1]);
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not copy artivact images for model creation!", e);
            }
        });
    }

}
