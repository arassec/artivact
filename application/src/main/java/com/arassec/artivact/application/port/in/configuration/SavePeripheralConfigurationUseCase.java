package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;

public interface SavePeripheralConfigurationUseCase {

    /**
     * Saves the configuration of all peripherals.
     *
     * @param peripheralConfiguration The configuration to save.
     */
    void savePeripheralConfiguration(PeripheralConfiguration peripheralConfiguration);

}
