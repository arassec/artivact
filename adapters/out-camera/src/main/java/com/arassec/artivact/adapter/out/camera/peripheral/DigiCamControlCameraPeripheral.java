package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.CameraPeripheral;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.peripheral.BasePeripheralAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Camera peripheral adapter for the Windows open source tool "DigiCamControl".
 * <p>
 * This implementation uses the <a href="https://digicamcontrol.com/doc/userguide/cmd">Command Line Utility</a>
 * of the application.
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class DigiCamControlCameraPeripheral extends BasePeripheralAdapter implements CameraPeripheral {

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(Path targetFile) {
        log.debug("Starting image capturing with DigiCamControl.");
        osGateway.execute(initParams.getConfiguration().getConfigValue(getSupportedImplementation()),
                List.of("/filename", targetFile.toString(), "/capture"));
        log.debug("Finished image capturing with DigiCamControl.");
        return true;
    }

}
