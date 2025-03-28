package com.arassec.artivact.domain.creator.adapter.image.camera;

import com.arassec.artivact.domain.creator.adapter.Adapter;

/**
 * Adapter definition for cameras.
 */
public interface CameraAdapter extends Adapter<CameraInitParams, Void> {

    /**
     * Captures an image with the camera.
     *
     * @param filename The target filename of the taken picture.
     * @return {@code true}, if capturing finished.
     */
    boolean captureImage(String filename);

}
