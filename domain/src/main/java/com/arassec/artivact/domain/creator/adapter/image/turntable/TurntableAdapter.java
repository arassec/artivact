package com.arassec.artivact.domain.creator.adapter.image.turntable;

import com.arassec.artivact.domain.creator.adapter.Adapter;

/**
 * Adapter definition for automatic turntables that rotate the item while being captured by the camera.
 */
public interface TurntableAdapter extends Adapter<TurntableInitParams, Void> {

    /**
     * Rotates the turntable by 360° divided by the number of photos.
     *
     * @param numPhotos The total number of photos that are taken during the session.
     */
    void rotate(int numPhotos);

}
