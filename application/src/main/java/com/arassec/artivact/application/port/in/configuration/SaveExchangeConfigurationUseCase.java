package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;

/**
 * Use case for save exchange configuration operations.
 */
public interface SaveExchangeConfigurationUseCase {

    /**
     * Saves an exchange configuration.
     *
     * @param exchangeConfiguration The configuration to save.
     */
    void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration);

}
