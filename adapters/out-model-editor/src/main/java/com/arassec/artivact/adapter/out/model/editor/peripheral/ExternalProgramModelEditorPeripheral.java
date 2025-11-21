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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    private final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

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
        List<PeripheralConfig> peripheralConfigs = new ArrayList<>();

        if (inUse.get()) {
            return peripheralConfigs;
        }

        return Stream.of("4.5", "5.0")
                .map(this::scanBlender)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(config -> (PeripheralConfig) config)
                .toList();
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

    /**
     * Scans for Blender 3D as model editor.
     *
     * @param version The blender version to search for, e.g. '4.5' or '5.0'.
     * @return A Blender peripheral configuration if available.
     */
    private Optional<ExternalProgramPeripheralConfig> scanBlender(String version) {
        if (osGateway.isLinux()) {
            Path home = Path.of(System.getProperty("user.home"));
            Optional<Path> optionalBlender = osGateway.scanForDirectory(home, 5, "blender-" + version);
            if (optionalBlender.isPresent()) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);
                peripheralConfig.setLabel("Blender " + version);
                peripheralConfig.setCommand(optionalBlender.get().resolve("blender").toAbsolutePath().toString());
                peripheralConfig.setArguments("--python {projectDir}/utils/Blender/blender-artivact-import.py\n-- {modelDir}");
                peripheralConfig.setFavourite(true);
                return Optional.of(peripheralConfig);
            }
        } else if (osGateway.isWindows()) {
            Path blenderPath = Path.of("C:\\Program Files\\Blender Foundation\\Blender " + version + "\\blender.exe");
            if (osGateway.isExecutable(blenderPath.toAbsolutePath().toString())) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);
                peripheralConfig.setLabel("Blender " + version);
                peripheralConfig.setCommand(blenderPath.toAbsolutePath().toString());
                peripheralConfig.setArguments("--python {projectDir}/utils/Blender/blender-artivact-import.py\n-- {modelDir}");
                peripheralConfig.setFavourite(true);
                return Optional.of(peripheralConfig);
            }
        }
        return Optional.empty();
    }

}
