package com.arassec.artivact.backend.service.creator.adapter.model.editor;

import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.CreationModelSet;
import com.arassec.artivact.backend.service.util.CmdUtil;
import com.arassec.artivact.backend.service.util.FileUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Adapter that starts the open source tool "Blender 3D" for model editing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class BlenderModelEditorAdapter extends BaseModelEditorAdapter {

    /**
     * The directory containing blender python scripts.
     */
    private static final String BLENDER_DIR = "utils/Blender";

    /**
     * The implementation supported by this adapter.
     */
    private final AdapterImplementation supportedImplementation = AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER;

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
    public void open(CreationModelSet creationModelSet) {
        var modelPath = initParams.getProjectRoot().resolve(creationModelSet.getDirectory());

        var blenderProjectExists = new AtomicBoolean(false);
        var blenderProjectFile = new StringBuilder();
        checkBlenderFile(modelPath, blenderProjectExists, blenderProjectFile);

        var cmdLine = new CommandLine(initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation()));

        if (blenderProjectExists.get()) {
            cmdLine.addArgument(blenderProjectFile.toString());
        } else {
            cmdLine.addArgument("--python");
            cmdLine.addArgument(initParams.getProjectRoot().resolve(BLENDER_DIR).resolve("blender-obj-import.py")
                    .toAbsolutePath().toString());
            cmdLine.addArgument("--");
            cmdLine.addArgument(modelPath.toAbsolutePath().toString());
        }

        cmdUtil.execute(cmdLine);
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
     * Searches for the Blender 3D project file and attaches it to the blender project file target parameter.
     *
     * @param modelPath                  Path to the model files.
     * @param blenderProjectExistsTarget Will be set to {@code true} if a Blender project file is found in the directory.
     * @param blenderProjectFileTarget   Will be appended by the path to the Blender project file.
     */
    private void checkBlenderFile(Path modelPath, AtomicBoolean blenderProjectExistsTarget,
                                  StringBuilder blenderProjectFileTarget) {
        try (Stream<Path> stream = Files.list(modelPath)) {
            stream.forEach(path -> {
                if (path.toString().endsWith(".blend")) {
                    blenderProjectExistsTarget.set(true);
                    blenderProjectFileTarget.append(path.toAbsolutePath());
                }
            });
        } catch (IOException e) {
            throw new ArtivactException("Could not determine blender project status!", e);
        }
    }

}
