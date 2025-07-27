package com.arassec.artivact.application.port.out.peripheral;

import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter definition for 3D model creation.
 */
public interface ModelCreatorPeripheral extends Peripheral {

    /**
     * Creates a 3D model using the adapter's underlying photogrammetry tool.
     *
     * @param images The image-sets to use as model input.
     * @return The result of the model creation containing the path to the created files.
     */
    ModelCreationResult createModel(List<Path> images);

}
