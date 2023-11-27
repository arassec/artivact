package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;

/**
 * Adapter definition for cameras.
 */
public interface CameraAdapter extends Adapter<CameraInitParams, Void> {

    /**
     * Captures an image with the camera.
     *
     * @param filename The target filename of the taken picture.
     */
    void captureImage(String filename);

}
