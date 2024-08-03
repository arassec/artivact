package com.arassec.artivact.domain.creator.adapter.model.editor;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.configuration.AdapterImplementation;
import com.arassec.artivact.core.model.item.CreationModelSet;
import com.arassec.artivact.core.repository.FileRepository;
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
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public AdapterImplementation getSupportedImplementation() {
        return AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER;
    }

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

        progressMonitor.updateLabelKey("editModelStart");

        execute(cmdLine);
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
