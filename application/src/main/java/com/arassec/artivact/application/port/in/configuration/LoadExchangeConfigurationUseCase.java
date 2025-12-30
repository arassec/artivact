package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;

/**
 * Use case for load exchange configuration operations.
 */
public interface LoadExchangeConfigurationUseCase {

    /**
     * Loads the current exchange configuration of the application.
     *
     * @return The current exchange configuration.
     */
    ExchangeConfiguration loadExchangeConfiguration();

}
