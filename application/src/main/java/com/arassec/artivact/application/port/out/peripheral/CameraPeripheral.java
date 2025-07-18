package com.arassec.artivact.application.port.out.peripheral;

import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;

public interface CameraPeripheral extends PeripheralAdapter {

    /**
     * Captures an image with the camera.
     *
     * @param filename The target filename of the taken picture.
     * @return {@code true}, if capturing finished.
     */
    boolean captureImage(String filename);

}
