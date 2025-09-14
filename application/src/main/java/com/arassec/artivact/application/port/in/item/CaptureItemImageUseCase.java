package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.media.CaptureImagesParams;

/**
 * Defines the use case for capturing images for an item.
 */
public interface CaptureItemImageUseCase {

    /**
     * Captures a single image to the item's image directory.
     *
     * @param itemId           The item's ID.
     * @param removeBackground Set to {@code true}, to remove the images background.
     * @return The captured image's filename.
     */
    String captureImage(String itemId, boolean removeBackground);

    /**
     * Captures images using a peripheral adapter for the given item.
     *
     * @param itemId              The item's ID.
     * @param captureImagesParams Parameters for image capturing.
     */
    void captureImages(String itemId, CaptureImagesParams captureImagesParams);

}
