package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;

import java.util.Map;

/**
 * Use case to test an unsaved peripheral configuration.
 */
public interface TestPeripheralsConfigurationUseCase {

    /**
     * Tests the supplied peripheral config.
     *
     * @param peripheralConfig The config to test.
     * @return A {@link PeripheralStatus} for the supplied config values.
     */
    PeripheralStatus testConfig(PeripheralConfig peripheralConfig);

    /**
     * Tests the supplied peripheral configurations.
     *
     * @param peripheralsConfiguration All peripheral configurations to test.
     * @return The status, indexed by the peripheral configuration's IDs.
     */
    Map<String, PeripheralStatus> testConfigs(PeripheralsConfiguration peripheralsConfiguration);

}
