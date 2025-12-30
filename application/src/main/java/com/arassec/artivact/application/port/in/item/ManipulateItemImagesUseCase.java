package com.arassec.artivact.application.port.in.item;

/**
 * Use case for manipulate item images operations.
 */
public interface ManipulateItemImagesUseCase {

    /**
     * Removes the backgrounds from the images of an item using the specified image manipulator configuration.
     *
     * @param itemId                   The ID of the item.
     * @param imageManipulatorConfigId The ID of the image manipulator configuration to use.
     * @param imageSetIndex            The index of the image set to process.
     */
    void removeBackgrounds(String itemId, String imageManipulatorConfigId, int imageSetIndex);

}
