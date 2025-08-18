package com.arassec.artivact.domain.model.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Peripheral initialization parameters.
 */
@Getter
@Builder
public class PeripheralInitParams {

    /**
     * The project's root directory.
     */
    private Path projectRoot;

    /**
     * Directory to use for files created / modified by the peripheral.
     */
    private Path workDir;

    /**
     * The peripheral configuration.
     */
    private PeripheralConfiguration configuration;

}
