package com.arassec.artivact.domain.model.peripheral;

/**
 * Defines the status of a peripheral.
 */
public enum PeripheralStatus {

    /**
     * The peripheral is available.
     */
    AVAILABLE,

    /**
     * The peripheral is in error state.
     */
    ERROR,

    /**
     * The peripheral is disconnected.
     */
    DISCONNECTED,

    /**
     * A program used by the peripheral is not executable.
     */
    NOT_EXECUTABLE,

    /**
     * A file used by the peripheral does not exist.
     */
    FILE_DOESNT_EXIST,

}
