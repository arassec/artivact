package com.arassec.artivact.backend.service.creator.adapter;

/**
 * Available adapter implementations.
 */
public enum AdapterImplementation {

    /**
     * Fallback for background removal.
     */
    FALLBACK_BACKGROUND_REMOVAL_ADAPTER,

    /**
     * Remote rembg background removal.
     */
    REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER,

    /**
     * Fallback camera adapter.
     */
    FALLBACK_CAMERA_ADAPTER,

    /**
     * DigiCamControl adapter.
     */
    DIGI_CAM_CONTROL_CAMERA_ADAPTER,

    /**
     * Remote DigiCamControl adapter.
     */
    DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER,

    /**
     * gphoto2 adapter.
     */
    GPHOTO_TWO_CAMERA_ADAPTER,

    /**
     * Fallback turntable.
     */
    FALLBACK_TURNTABLE_ADAPTER,

    /**
     * Artivact turntable adapter.
     */
    ARTIVACT_TURNTABLE_ADAPTER,

    /**
     * Fallback model creator.
     */
    FALLBACK_MODEL_CREATOR_ADAPTER,

    /**
     * Meshroom adapter.
     */
    MESHROOM_MODEL_CREATOR_ADAPTER,

    /**
     * Metashape adapter.
     */
    METASHAPE_MODEL_CREATOR_ADAPTER,

    /**
     * RealityCapture adapter.
     */
    REALITY_CAPTURE_MODEL_CREATOR_ADAPTER,

    /**
     * Fallback model editor.
     */
    FALLBACK_MODEL_EDITOR_ADAPTER,

    /**
     * Blender 3D adapter.
     */
    BLENDER_MODEL_EDITOR_ADAPTER

}
