package com.arassec.artivact.domain.creator.adapter.image.camera;

import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.creator.adapter.AdapterInitParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

/**
 * Contains parameters for camera initialization.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CameraInitParams extends AdapterInitParams {

    /**
     * The current adapter configuration.
     */
    private AdapterConfiguration adapterConfiguration;

    /**
     * The target directory for captured images.
     */
    private Path targetDir;

}
