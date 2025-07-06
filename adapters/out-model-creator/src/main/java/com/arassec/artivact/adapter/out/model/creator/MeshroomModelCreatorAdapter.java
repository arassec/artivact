package com.arassec.artivact.adapter.out.model.creator;

import com.arassec.artivact.domain.model.adapter.ModelCreationResult;
import com.arassec.artivact.domain.model.configuration.AdapterImplementation;
import com.arassec.artivact.application.port.out.repository.FileRepository;
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
     * The file repository.
     */
    private final FileRepository fileRepository;

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
        Path tempDir = initParams.getWorkDir();

        Path resultDir = tempDir.resolve(EXPORT_DIR).toAbsolutePath();
        Path cacheDir = projectRoot.resolve(MESHROOM_DIR).resolve(MESHROOM_CACHE_DIR);

        fileRepository.emptyDir(tempDir);
        fileRepository.emptyDir(resultDir);
        fileRepository.emptyDir(cacheDir);

        copyImages(images, tempDir, progressMonitor);

        fileRepository.openDirInOs(tempDir);

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));
        cmdLine.addArgument(projectRoot.resolve(MESHROOM_DIR).resolve("artivact-meshroom-workflow.mg").toAbsolutePath().toString());

        progressMonitor.updateLabelKey("createModelStart");

        execute(cmdLine);

        return new ModelCreationResult(resultDir, "meshroom");
    }

}
