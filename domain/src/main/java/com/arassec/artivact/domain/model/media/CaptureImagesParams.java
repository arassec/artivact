package com.arassec.artivact.domain.model.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for iamge capturing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureImagesParams {

    /**
     * The number of photos to capture.
     */
    private int numPhotos;

    /**
     * Set to {@code true}, if a turntable should be used.
     */
    private boolean useTurnTable;

    /**
     * Set to {@code true} if backgrounds should automatically be removed from captured images.
     */
    private boolean removeBackgrounds;

    private String cameraPeripheralConfigId;

    private String turntablePeripheralConfigId;

    private String imageBackgroundRemovalPeripheralConfigId;

}
