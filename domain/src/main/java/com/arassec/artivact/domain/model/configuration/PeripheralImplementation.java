package com.arassec.artivact.domain.model.configuration;

/**
 * Available peripheral implementations.
 */
public enum PeripheralImplementation {

    /**
     * Turntable controlled by Arduino with Firmata4J.
     */
    ARDUINO_TURNTABLE_PERIPHERAL,

    /**
     * Picture-Transfer-Protocol camera peripheral.
     */
    PTP_CAMERA_PERIPHERAL,

    /**
     * "Camera controlled by an external program" peripheral.
     */
    EXTERNAL_PROGRAM_CAMERA_PERIPHERAL,

    /**
     * Image background removal with ONNX networks.
     */
    ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL,

    /**
     * "External program for model creation" peripheral.
     */
    EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL,

    /**
     * "External program for model editing" peripheral.
     */
    EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL,

}
