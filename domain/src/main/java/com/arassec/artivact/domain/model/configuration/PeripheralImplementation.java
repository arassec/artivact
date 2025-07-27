package com.arassec.artivact.domain.model.configuration;

/**
 * Available peripheral implementations.
 */
public enum PeripheralImplementation {

    /**
     * Default turntable.
     */
    DEFAULT_TURNTABLE_PERIPHERAL,

    /**
     * Default for background removal.
     */
    DEFAULT_IMAGE_MANIPULATION_PERIPHERAL,

    /**
     * Default camera peripheral.
     */
    DEFAULT_CAMERA_PERIPHERAL,

    /**
     * DigiCamControl peripheral.
     */
    DIGI_CAM_CONTROL_CAMERA_PERIPHERAL,

    /**
     * gphoto2 peripheral.
     */
    GPHOTO_TWO_CAMERA_PERIPHERAL,

    /**
     * Fallback model creator.
     */
    FALLBACK_MODEL_CREATOR_PERIPHERAL,

    /**
     * Meshroom peripheral.
     */
    MESHROOM_MODEL_CREATOR_PERIPHERAL,

    /**
     * Metashape peripheral.
     */
    METASHAPE_MODEL_CREATOR_PERIPHERAL,

    /**
     * RealityScan peripheral.
     */
    REALITY_SCAN_MODEL_CREATOR_PERIPHERAL,

    /**
     * Fallback model editor.
     */
    FALLBACK_MODEL_EDITOR_PERIPHERAL,

    /**
     * Blender 3D peripheral.
     */
    BLENDER_MODEL_EDITOR_PERIPHERAL

}
