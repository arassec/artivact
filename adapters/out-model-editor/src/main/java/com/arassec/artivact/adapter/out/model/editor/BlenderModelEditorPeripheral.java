package com.arassec.artivact.adapter.out.model.editor;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Adapter that starts the open source tool "Blender 3D" for model editing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class BlenderModelEditorPeripheral extends BasePeripheralAdapter implements ModelEditorPeripheral {

    /**
     * The directory containing blender python scripts.
     */
    private static final String BLENDER_DIR = "utils/Blender";

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL;
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

        String command = initParams.getAdapterConfiguration().getConfigValue(getSupportedImplementation());

        List<String> arguments = new ArrayList<>();
        if (blenderProjectExists.get()) {
            arguments.add(blenderProjectFile.toString());
        } else {
            arguments.add("--python");
            arguments.add(initParams.getProjectRoot().resolve(BLENDER_DIR).resolve("blender-obj-import.py")
                    .toAbsolutePath().toString());
            arguments.add("--");
            arguments.add(modelPath.toAbsolutePath().toString());
        }

        progressMonitor.updateLabelKey("editModelStart");

        osGateway.execute(command, arguments);
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
