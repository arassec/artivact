package com.arassec.artivact.adapter.out.model.editor.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Adapter that starts the open source tool "Blender 3D" for model editing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramModelEditorPeripheral extends BasePeripheral implements ModelEditorPeripheral {

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }

        if (osGateway.isExecutable(((ExternalProgramPeripheralConfig) peripheralConfig).getCommand())) {
            return PeripheralStatus.AVAILABLE;
        }

        return PeripheralStatus.NOT_EXECUTABLE;
    }

    @Override
    public List<PeripheralConfig> scanPeripherals() {
        if (inUse.get()) {
            return List.of();
        }

        if (osGateway.isLinux()) {
            Path home = Path.of(System.getProperty("user.home"));
            Optional<Path> optionalBlender = osGateway.scanForDirectory(home, 5, "blender-4.5");
            if (optionalBlender.isPresent()) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);
                peripheralConfig.setLabel("Blender 4.5");
                peripheralConfig.setCommand(optionalBlender.get().resolve("blender").toAbsolutePath().toString());
                peripheralConfig.setArguments("--python {projectDir}/utils/Blender/blender-artivact-import.py\n-- {modelDir}");
                peripheralConfig.setFavourite(true);
                return List.of(peripheralConfig);
            }
        } else if (osGateway.isWindows()) {
            Path blenderPath = Path.of("C:\\Program Files\\Blender Foundation\\Blender 4.5\\blender.exe");
            if (osGateway.isExecutable(blenderPath.toAbsolutePath().toString())) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);
                peripheralConfig.setLabel("Blender 4.5");
                peripheralConfig.setCommand(blenderPath.toAbsolutePath().toString());
                peripheralConfig.setArguments("--python {projectDir}/utils/Blender/blender-artivact-import.py\n-- {modelDir}");
                peripheralConfig.setFavourite(true);
                return List.of(peripheralConfig);
            }
        }

        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(CreationModelSet creationModelSet) {
        ExternalProgramPeripheralConfig config = (ExternalProgramPeripheralConfig) initParams.getConfig();

        String command = config.getCommand();
        String[] arguments = {};

        if (config.getArguments() != null) {
            arguments = config.getArguments()
                    .replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString())
                    .replace("{modelDir}", initParams.getProjectRoot().resolve(creationModelSet.getDirectory()).toAbsolutePath().toString())
                    .replace("\n", " ")
                    .split(" ");
        }

        progressMonitor.updateLabelKey("start");

        osGateway.execute(command, Arrays.asList(arguments));
    }

}
