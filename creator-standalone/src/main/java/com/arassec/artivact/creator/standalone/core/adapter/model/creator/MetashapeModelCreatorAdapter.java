package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.adapter.BaseAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.service.ProjectService;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "adapter.implementation.model-creator", havingValue = "Metashape")
public class MetashapeModelCreatorAdapter extends BaseAdapter implements ModelCreatorAdapter {

    private final ProjectService projectService;

    private final FileUtil fileUtil;

    private final MessageSource messageSource;

    @Value("${adapter.implementation.model-creator.executable}")
    private String executable;

    @Override
    public String getDefaultPipeline() {
        return null;
    }

    @Override
    public List<String> getPipelines() {
        return List.of();
    }

    @Override
    public void createModel(CreatorArtivact creatorArtivact, String pipeline, ProgressMonitor progressMonitor) {
        Path projectRoot = creatorArtivact.getProjectRoot();

        Path inputDir = creatorArtivact.getProjectTempDir();
        Path resultDir = Path.of(creatorArtivact.getProjectTempDir().toAbsolutePath().toString(), "/metashape-export/");

        fileUtil.emptyDir(inputDir);

        creatorArtivact.getImageSets().forEach(imageSet -> {
            if (imageSet.isModelInput()) {
                copyImages(creatorArtivact, imageSet, inputDir, progressMonitor);
            }
        });

        projectService.getActiveCreatorArtivact().openDirInOs(inputDir);

        var cmdLine = new CommandLine(executable);

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        try {
            progressMonitor.setProgress(messageSource.getMessage("model-creator-adapter.metashape.progress.prefix", null, Locale.getDefault()));
            executor.execute(cmdLine, resultHandler);

            resultHandler.waitFor();

            if (resultHandler.getExitValue() != 0 && resultHandler.getException() != null) {
                log.error("Could not open Metashape!", resultHandler.getException());
            } else {
                progressMonitor.setProgress(messageSource.getMessage("model-creator-adapter.metashape.import.progress.prefix", null, Locale.getDefault()));
                creatorArtivact.createModel(resultDir, "metashape");
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not open model in Blender!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactCreatorException("Interrupted during Blender session!", e);
        }
    }

    @Override
    public void cancelModelCreation() {
        throw new ArtivactCreatorException("Cancellation is not supported by " + this.getClass().getSimpleName());
    }

    @Override
    public boolean supportsCancellation() {
        return false;
    }
}
