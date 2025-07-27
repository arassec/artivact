package com.arassec.artivact.application.port.out.peripheral;

import com.arassec.artivact.domain.model.peripheral.Peripheral;

import java.nio.file.Path;

public interface CameraPeripheral extends Peripheral {

    /**
     * Captures an image with the camera.
     *
     * @param targetFile The target file of the captured picture.
     * @return {@code true}, if capturing finished.
     */
    boolean captureImage(Path targetFile);

}
