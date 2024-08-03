package com.arassec.artivact.domain.creator.adapter.image.background;

import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.creator.adapter.AdapterInitParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

/**
 * Initialization parameters for background removal adapters.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BackgroundRemovalInitParams extends AdapterInitParams {

    /**
     * The adapter configuration.
     */
    private AdapterConfiguration adapterConfiguration;

    /**
     * Target dir for images with removed background.
     */
    private Path targetDir;

}
