package com.arassec.artivact.domain.creator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for photo capturing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturePhotosParams {

    /**
     * The number of photos to capture.
     */
    private int numPhotos;

    /**
     * Set to {@code true}, if a turntable should be used.
     */
    private boolean useTurnTable;

    /**
     * Delay for turntable usage in milliseconds.
     */
    private int turnTableDelay;

    /**
     * Set to {@code true} if backgrounds should automatically be removed from captured images.
     */
    private boolean removeBackgrounds;

}
