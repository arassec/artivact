package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation for the open source tool "Meshroom".
 */
@Component
@Getter
@RequiredArgsConstructor
public class MeshroomModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The default pipeline as provide by Artivact-Creator.
     */
    public static final String DEFAULT_PIPELINE = "meshroom-200k-4.mg";

    /**
     * All available pipelines as provided by Artivact-Creator.
     */
    public static final List<String> PIPELINES = List.of(DEFAULT_PIPELINE, "meshroom-200k-2.mg", "meshroom-default.mg");

    /**
     * The directory containing pipeline files.
     */
    private static final String MESHROOM_DIR = "utils/Meshroom";

    /**
     * The cache directory of Meshroom.
     */
    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

    /**
     * The result dir of Meshroom.
     */
    private static final String MESHROOM_RESULT_DIR = "MeshroomResult";

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER;

    /**
     * The commandline util.
     */
    private final CmdUtil cmdUtil;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getDefaultPipeline() {
        return Optional.of(DEFAULT_PIPELINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPipelines() {
        return PIPELINES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images, String pipeline) {

        Path projectRoot = initParams.getProjectRoot();
        Path tempDir = initParams.getTempDir();

        Path resultDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_RESULT_DIR);
        Path cacheDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_CACHE_DIR);

        fileUtil.emptyDir(tempDir);
        fileUtil.emptyDir(resultDir);
        fileUtil.emptyDir(cacheDir);

        copyImages(images, tempDir, progressMonitor);

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument("-i");
        cmdLine.addArgument(tempDir.toAbsolutePath().toString());
        cmdLine.addArgument("-p");
        cmdLine.addArgument(projectRoot.resolve(MESHROOM_DIR).resolve(pipeline).toAbsolutePath().toString());
        cmdLine.addArgument("-o");
        cmdLine.addArgument(resultDir.toAbsolutePath().toString());
        cmdLine.addArgument("--cache");
        cmdLine.addArgument(cacheDir.toAbsolutePath().toString());

        progressMonitor.updateLabelKey("createModelStart");

        cmdUtil.execute(cmdLine);

        return new ModelCreationResult(resultDir, pipeline);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelModelCreation() {
        killProcesses("meshroom");
        killProcesses("aliceVision");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsCancellation() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration(AdapterConfiguration adapterConfiguration) {
        if (!cmdUtil.execute(new CommandLine(adapterConfiguration.getConfigValue(getSupportedImplementation())))) {
            throw new ArtivactException("Configured file could not be executed!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean healthy(AdapterConfiguration adapterConfiguration) {
        try {
            return fileUtil.isFileExecutable(adapterConfiguration.getConfigValue(getSupportedImplementation()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kills the process with the given name.
     *
     * @param processName The name of the process to kill.
     */
    private void killProcesses(String processName) {
        List<ProcessHandle> runningMeshroomProcesses = ProcessHandle.allProcesses()
                .filter(p -> p.info().command().map(c -> c.contains(processName)).orElse(false))
                .toList();
        if (!runningMeshroomProcesses.isEmpty()) {
            runningMeshroomProcesses.forEach(ProcessHandle::destroy);
        }
    }

}
