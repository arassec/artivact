package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.AdapterConfiguration;

public interface SaveAdapterConfigurationUseCase {

    /**
     * Saves an adapter configuration.
     *
     * @param adapterConfiguration The configuration to save.
     */
    void saveAdapterConfiguration(AdapterConfiguration adapterConfiguration);

}
