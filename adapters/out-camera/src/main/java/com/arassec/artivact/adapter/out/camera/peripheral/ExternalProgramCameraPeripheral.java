package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
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

/**
 * Camera peripheral that uses an external program to capture images..
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramCameraPeripheral extends BasePeripheral implements CameraPeripheral {

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PeripheralConfig> scanPeripherals() {
        if (inUse.get()) {
            return List.of();
        }

        if (osGateway.isLinux()) {
            // Search for GPhoto2
            Path command = Path.of("/usr/bin/gphoto2");
            if (osGateway.isExecutable(command.toString())) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
                peripheralConfig.setLabel("gphoto2 Camera");
                peripheralConfig.setCommand(command.toAbsolutePath().toString());
                peripheralConfig.setArguments("--filename {targetFile}\n--capture-image-and-download");
                return List.of(peripheralConfig);
            }
        } else if (osGateway.isWindows()) {
            // Search for DigiCamControl
            Path command = Path.of("C:\\Program Files (x86)\\digiCamControl\\CameraControlCmd.exe");
            if (osGateway.isExecutable(command.toString())) {
                ExternalProgramPeripheralConfig peripheralConfig = new ExternalProgramPeripheralConfig();
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
                peripheralConfig.setLabel("DigiCamControl Camera");
                peripheralConfig.setCommand(command.toAbsolutePath().toString());
                peripheralConfig.setArguments("/filename {targetFile}\n/capture");
                return List.of(peripheralConfig);
            }
        }

        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(Path targetFile) {
        String command = ((ExternalProgramPeripheralConfig) initParams.getConfig()).getCommand();
        String[] arguments = ((ExternalProgramPeripheralConfig) initParams.getConfig()).getArguments()
                .replace("{targetFile}", targetFile.toString())
                .replace("\n", " ")
                .split(" ");
        if (!osGateway.execute(command, Arrays.asList(arguments))) {
            throw new ArtivactException("Could not capture image (command execution failed)!");
        }
        log.debug("Finished image capturing with external program.");
        return true;
    }

}
