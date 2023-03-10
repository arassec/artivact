package com.arassec.artivact.creator.standalone.core.adapter.model.creator;

import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.adapter.BaseAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
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
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "adapter.implementation.model-creator", havingValue = "Meshroom")
public class MeshroomModelCreatorAdapter extends BaseAdapter implements ModelCreatorAdapter {

    public static final String DEFAULT_PIPELINE = "meshroom-200k-4.mg";

    public static final List<String> PIPELINES = List.of(DEFAULT_PIPELINE, "meshroom-200k-2.mg", "meshroom-default.mg");

    private static final String MESHROOM_DIR = "/Utils/Meshroom";

    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

    private static final String MESHROOM_RESULT_DIR = "MeshroomResult";

    private final MessageSource messageSource;

    private final FileUtil fileUtil;

    @Value("${adapter.implementation.model-creator.executable}")
    private String executable;

    @Override
    public String getDefaultPipeline() {
        return DEFAULT_PIPELINE;
    }

    @Override
    public List<String> getPipelines() {
        return PIPELINES;
    }

    public void createModel(CreatorArtivact creatorArtivact, String pipeline, ProgressMonitor progressMonitor) {
        Path projectRoot = creatorArtivact.getProjectRoot();

        Path inputDir = creatorArtivact.getProjectTempDir();
        Path resultDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_RESULT_DIR);
        Path cacheDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_CACHE_DIR);

        fileUtil.emptyDir(inputDir);
        fileUtil.emptyDir(resultDir);
        fileUtil.emptyDir(cacheDir);

        creatorArtivact.getImageSets().forEach(imageSet -> {
            if (imageSet.isModelInput()) {
                copyImages(creatorArtivact, imageSet, inputDir, progressMonitor);
            }
        });

        try {
            var cmdLine = new CommandLine(executable);
            cmdLine.addArgument("-i");
            cmdLine.addArgument(inputDir.toAbsolutePath().toString());
            cmdLine.addArgument("-p");
            cmdLine.addArgument(projectRoot.resolve(MESHROOM_DIR).resolve(pipeline).toAbsolutePath().toString());
            cmdLine.addArgument("-o");
            cmdLine.addArgument(resultDir.toAbsolutePath().toString());
            cmdLine.addArgument("--cache");
            cmdLine.addArgument(cacheDir.toAbsolutePath().toString());

            var resultHandler = new DefaultExecuteResultHandler();

            progressMonitor.setProgress(messageSource.getMessage("model-creator-adapter.meshroom.progress.prefix", null, Locale.getDefault()));

            Executor executor = new DaemonExecutor();
            executor.setExitValue(1);
            executor.execute(cmdLine, resultHandler);

            // some time later the result handler callback was invoked so we
            // can safely request the exit value
            resultHandler.waitFor();

            creatorArtivact.createModel(resultDir, pipeline);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not create 3D model!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactCreatorException("Interrupted during model creation!", e);
        }
    }

    public void cancelModelCreation() {
        killProcesses("meshroom");
        killProcesses("aliceVision");
    }

    @Override
    public boolean supportsCancellation() {
        return true;
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private void killProcesses(String processName) {
        List<ProcessHandle> runningMeshroomProcesses = ProcessHandle.allProcesses()
                .filter(p -> p.info().command().map(c -> c.contains(processName)).orElse(false))
                .collect(Collectors.toList());
        if (!runningMeshroomProcesses.isEmpty()) {
            runningMeshroomProcesses.forEach(ProcessHandle::destroy);
        }
    }

}
