package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;

/**
 * Use case for load exchange configuration operations.
 */
public interface LoadExchangeConfigurationUseCase {

    ExchangeConfiguration loadExchangeConfiguration();

}
