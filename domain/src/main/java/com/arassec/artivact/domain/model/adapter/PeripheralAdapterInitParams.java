package com.arassec.artivact.domain.model.adapter;

import com.arassec.artivact.domain.model.configuration.AdapterConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Base for adapter initialization parameters.
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
    private AdapterConfiguration adapterConfiguration;

    /**
     * Directory to use for files created / modified by the adapter.
     */
    private Path workDir;

}
