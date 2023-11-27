package com.arassec.artivact.backend.service.creator.adapter.image.background;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter definition for automatically removing the background from images.
 */
public interface BackgroundRemovalAdapter extends Adapter<BackgroundRemovalInitParams, List<Path>> {

    /**
     * Removes the background from a single image.
     *
     * @param filePath  Path to the image file.
     */
    void removeBackground(Path filePath);

    /**
     * Removes the background from all images of the supplied image set in batch.
     *
     * @param imageSet  The image set containing the images to process.
     */
    void removeBackgrounds(ImageSet imageSet);

}
