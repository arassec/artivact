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
 * Camera adapter that uses the Linux command line tool "gphoto2".
 */
@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class GPhotoTwoCameraPeripheral extends BasePeripheralAdapter implements CameraPeripheral {

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureImage(Path targetFile) {
        osGateway.execute(initParams.getConfiguration().getConfigValue(getSupportedImplementation()),
                List.of("--filename", targetFile.toString(), "--capture-image-and-download"));
        return true;
    }

}
