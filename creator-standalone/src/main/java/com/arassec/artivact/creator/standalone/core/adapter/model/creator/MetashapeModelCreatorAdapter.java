package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.creator.standalone.core.model.Artivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.service.ProjectService;
import com.arassec.artivact.creator.standalone.core.util.FileHelper;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "adapter.implementation.model-creator", havingValue = "Metashape")
public class MetashapeModelCreatorAdapter implements ModelCreatorAdapter {

    private final ProjectService projectService;

    private final FileHelper fileHelper;

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
    public void createModel(Artivact artivact, String pipeline, ProgressMonitor progressMonitor) {
        Path projectRoot = artivact.getProjectRoot();

        Path inputDir = projectRoot.resolve(FileHelper.TEMP_DIR);
        Path resultDir = projectRoot.resolve(FileHelper.TEMP_DIR + "/metashape-export/");

        fileHelper.emptyDir(inputDir);

        artivact.getImageSets().forEach(imageSet -> {
            if (imageSet.isModelInput()) {
                fileHelper.copyImages(artivact, imageSet, inputDir, progressMonitor);
            }
        });

        projectService.getActiveArtivact().openDirInOs(inputDir);

        var cmdLine = new CommandLine(executable);

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        try {
            executor.execute(cmdLine, resultHandler);

            resultHandler.waitFor();

            if (resultHandler.getExitValue() != 0 && resultHandler.getException() != null) {
                log.error("Could not open Metashape!", resultHandler.getException());
            } else {
                artivact.createModel(resultDir, "metashape");
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

    }

}
