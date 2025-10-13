package com.arassec.artivact.application.port.in.item;

public interface ManipulateItemImagesUseCase {

    void removeBackgrounds(String itemId, String imageManipulatorConfigId, int imageSetIndex);

}
