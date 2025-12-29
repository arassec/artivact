package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;

/**
 * Use case for load peripherals configuration operations.
 */
public interface LoadPeripheralsConfigurationUseCase {

    PeripheralsConfiguration loadPeripheralConfiguration();

}
