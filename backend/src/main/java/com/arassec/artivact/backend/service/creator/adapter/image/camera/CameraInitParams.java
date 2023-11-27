package com.arassec.artivact.backend.service.creator.adapter.image.camera;

import com.arassec.artivact.backend.service.creator.adapter.AdapterInitParams;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import lombok.Builder;
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
