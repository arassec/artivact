package com.arassec.artivact.backend.service.creator.adapter.image.background;

import com.arassec.artivact.backend.service.creator.adapter.AdapterInitParams;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import lombok.Builder;
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
