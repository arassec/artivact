package com.arassec.artivact.domain.creator.adapter.model.creator;

import com.arassec.artivact.core.model.configuration.AdapterConfiguration;
import com.arassec.artivact.domain.creator.adapter.AdapterInitParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

/**
 * Initialization parameters for model creation.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ModelCreatorInitParams extends AdapterInitParams {

    /**
     * The adapters current configuration property.
     */
    private AdapterConfiguration adapterConfiguration;

    /**
     * Path to the temp directory.
     */
    private Path tempDir;

}
