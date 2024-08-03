package com.arassec.artivact.core.repository;

import com.arassec.artivact.core.model.configuration.ConfigurationType;

import java.util.Optional;

/**
 * Repository for the application's configuration.
 */
public interface ConfigurationRepository {

    /**
     * Returns the configuration of the given type.
     *
     * @param configurationType  The configuration's type.
     * @param configurationClazz The target class to return.
     * @param <T>                Type of the configuration.
     * @return An instance of the configuration class with the configured values.
     */
    <T> Optional<T> findByType(ConfigurationType configurationType, Class<T> configurationClazz);

    /**
     * Saves the given configuration.
     *
     * @param configurationType   The configuration's type.
     * @param configurationObject The configuration object to save.
     */
    void saveConfiguration(ConfigurationType configurationType, Object configurationObject);

}
