package com.arassec.artivact.application.port.in.item;

public interface CreateItemModelUseCase {

    /**
     * Creates a new 3D model for the item by starting an external photogrammetry program.
     *
     * @param itemId The item's ID.
     */
    void createModel(String itemId);

}
