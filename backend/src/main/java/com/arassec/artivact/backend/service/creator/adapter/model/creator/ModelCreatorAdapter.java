package com.arassec.artivact.backend.service.creator.adapter.model.creator;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;

import java.util.List;
import java.util.Optional;

/**
 * Adapter definition for 3D model creation.
 */
public interface ModelCreatorAdapter extends Adapter<ModelCreatorInitParams, Void> {

    /**
     * Returns a default pipeline to use for model creation.
     *
     * @return The default pipeline as String.
     */
    Optional<String> getDefaultPipeline();

    /**
     * Returns all available pipelines of this adapter.
     *
     * @return List of available pipelines for model creation.
     */
    List<String> getPipelines();

    /**
     * Creates a 3D model using the adapter's underlying photogrammetry tool.
     *
     * @param imageSets The image-sets to use as model input.
     * @param pipeline  The pipeline to use for model creation.
     * @return The result of the model creation containing the path to the created files.
     */
    ModelCreationResult createModel(List<ImageSet> imageSets, String pipeline);

    /**
     * Cancels the model creation if supported by the adapter.
     */
    void cancelModelCreation();

    /**
     * Indicates whether model creation can be cancelled or not.
     *
     * @return {@code true} if the creation can be cancelled, {@code false} otherwise.
     */
    boolean supportsCancellation();

}
