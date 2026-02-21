package com.arassec.artivact.domain.model.configuration;

/**
 * Marker interface for configuration containers that return their own configuration type.
 */
public interface ConfigurationTypeProvider {

    ConfigurationType getConfigurationType();

}
