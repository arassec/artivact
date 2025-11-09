package com.arassec.artivact.application.port.in.configuration;

/**
 * Scans for peripherals and updates the configuration accordingly.
 */
public interface ScanPeripheralsConfigurationUseCase {

    /**
     * Scans and saves found peripheral configurations.
     */
    void scanPeripheralsConfiguration();

}
