package com.arassec.artivact.application.port.out.adapter;

import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;

public interface CameraAdapter extends PeripheralAdapter {

    /**
     * Captures an image with the camera.
     *
     * @param filename The target filename of the taken picture.
     * @return {@code true}, if capturing finished.
     */
    boolean captureImage(String filename);

}
