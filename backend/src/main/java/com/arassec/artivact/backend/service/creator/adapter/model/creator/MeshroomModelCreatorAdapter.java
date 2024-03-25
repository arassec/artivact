package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter implementation for the open source tool "Meshroom".
 */
@Component
@Getter
@RequiredArgsConstructor
public class MeshroomModelCreatorAdapter extends BaseModelCreatorAdapter {

    /**
     * The directory containing pipeline files.
     */
    private static final String MESHROOM_DIR = "utils/Meshroom";

    /**
     * The cache directory of Meshroom.
     */
    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

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
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {

        Path projectRoot = initParams.getProjectRoot();
        Path tempDir = initParams.getTempDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();
        Path cacheDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_CACHE_DIR);

        fileUtil.emptyDir(tempDir);
        fileUtil.emptyDir(resultDir);
        fileUtil.emptyDir(cacheDir);

        copyImages(images, tempDir, progressMonitor);

        fileUtil.openDirInOs(tempDir);

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument(projectRoot.resolve(MESHROOM_DIR).resolve("artivact-meshroom-workflow.mg").toAbsolutePath().toString());

        progressMonitor.updateLabelKey("createModelStart");

        cmdUtil.execute(cmdLine);

        return new ModelCreationResult(resultDir, "meshroom");
    }

}
