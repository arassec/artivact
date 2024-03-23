package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter definition for 3D model creation.
 */
public interface ModelCreatorAdapter extends Adapter<ModelCreatorInitParams, Void> {

    /**
     * Creates a 3D model using the adapter's underlying photogrammetry tool.
     *
     * @param images The image-sets to use as model input.
     * @return The result of the model creation containing the path to the created files.
     */
    ModelCreationResult createModel(List<Path> images);

}
