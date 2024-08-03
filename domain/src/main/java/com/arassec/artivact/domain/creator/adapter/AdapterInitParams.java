package com.arassec.artivact.domain.creator.adapter;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

/**
 * Base for adapter initialization parameters.
 */
@Getter
@SuperBuilder
public abstract class AdapterInitParams {

    /**
     * The project's root directory.
     */
    private Path projectRoot;

}
