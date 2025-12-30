package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;

/**
 * Use case for save peripheral configuration operations.
 */
public interface SavePeripheralConfigurationUseCase {

    /**
     * Saves the configuration of all peripherals.
     *
     * @param peripheralConfiguration The configuration to save.
     */
    PeripheralsConfiguration savePeripheralConfiguration(PeripheralsConfiguration peripheralConfiguration);

}
