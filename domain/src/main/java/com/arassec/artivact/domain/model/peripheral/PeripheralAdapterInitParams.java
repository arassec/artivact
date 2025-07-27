package com.arassec.artivact.domain.model.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Base for peripheral initialization parameters.
 */
@Getter
@Builder
public class PeripheralAdapterInitParams {

    /**
     * The project's root directory.
     */
    private Path projectRoot;

    /**
     * The adapter configuration.
     */
    private PeripheralConfiguration adapterConfiguration;

    /**
     * Directory to use for files created / modified by the adapter.
     */
    private Path workDir;

}
