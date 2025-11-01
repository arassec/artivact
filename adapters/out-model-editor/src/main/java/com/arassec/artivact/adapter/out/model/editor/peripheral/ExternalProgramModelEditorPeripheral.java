package com.arassec.artivact.adapter.out.model.editor.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.ModelEditorPeripheral;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Adapter that starts the open source tool "Blender 3D" for model editing.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramModelEditorPeripheral extends BasePeripheralAdapter implements ModelEditorPeripheral {

    /**
     * The directory containing blender python scripts.
     */
    private static final String BLENDER_DIR = "utils/Blender";

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
