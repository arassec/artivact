package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.media.CreateModelParams;

/**
 * Use case for create item model operations.
 */
public interface CreateItemModelUseCase {

    /**
     * Creates a new 3D model for the item by starting an external photogrammetry program.
     *
     * @param itemId            The item's ID.
     * @param createModelParams Parameters for model creation.
     */
    void createModel(String itemId, CreateModelParams createModelParams);

}
