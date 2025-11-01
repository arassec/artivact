package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Camera peripheral that uses an external program to capture images..
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramCameraPeripheral extends BasePeripheralAdapter implements CameraPeripheral {

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
    public boolean captureImage(Path targetFile) {
        String command = ((ExternalProgramPeripheralConfig) initParams.getConfig()).getCommand();
        String[] arguments = ((ExternalProgramPeripheralConfig) initParams.getConfig()).getArguments()
                .replace("{targetFile}", targetFile.toString())
                .replace("\n", " ")
                .split(" ");
        osGateway.execute(command, Arrays.asList(arguments));
        log.debug("Finished image capturing with external program.");
        return true;
    }

}
