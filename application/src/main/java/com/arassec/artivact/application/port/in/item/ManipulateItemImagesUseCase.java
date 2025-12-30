package com.arassec.artivact.application.port.in.item;

/**
 * Use case for manipulate item images operations.
 */
public interface ManipulateItemImagesUseCase {

    void removeBackgrounds(String itemId, String imageManipulatorConfigId, int imageSetIndex);

}
