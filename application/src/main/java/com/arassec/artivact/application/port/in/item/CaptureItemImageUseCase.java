package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.media.CaptureImagesParams;

/**
 * Defines the use case for capturing images for an item.
 */
public interface CaptureItemImageUseCase {

    /**
     * Captures images using a peripheral adapter for the given item.
     *
     * @param itemId              The item's ID.
     * @param captureImagesParams Parameters for image capturing.
     */
    void capture(String itemId, CaptureImagesParams captureImagesParams);

}
