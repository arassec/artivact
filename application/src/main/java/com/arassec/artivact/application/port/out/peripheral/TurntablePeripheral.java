package com.arassec.artivact.application.port.out.peripheral;

import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;

/**
 * Adapter definition for automatic turntables that rotate the item while being captured by the camera.
 */
public interface TurntablePeripheral extends PeripheralAdapter {

    /**
     * Rotates the turntable by 360° divided by the number of photos.
     *
     * @param numPhotos The total number of photos that are taken during the session.
     * @param delay     Delay in milliseconds before rotation.
     */
    void rotate(int numPhotos, int delay);

}
