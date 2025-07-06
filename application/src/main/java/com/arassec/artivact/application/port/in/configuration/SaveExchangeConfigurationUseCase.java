package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;

public interface SaveExchangeConfigurationUseCase {

    /**
     * Saves an exchange configuration.
     *
     * @param exchangeConfiguration The configuration to save.
     */
    void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration);

}
