package com.arassec.artivact.application.port.in.configuration;

import com.arassec.artivact.domain.model.exchange.ImportContext;

public interface ImportPropertiesConfigurationUseCase {

    void importPropertiesConfiguration(String propertiesConfiguration);

    /**
     * Imports the properties configuration file located in the import directory.
     *
     * @param importContext The import context containing the import directory.
     */
    void importPropertiesConfiguration(ImportContext importContext);

}
