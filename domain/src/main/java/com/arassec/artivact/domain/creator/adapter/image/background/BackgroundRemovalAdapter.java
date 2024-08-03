package com.arassec.artivact.domain.creator.adapter.image.background;

import com.arassec.artivact.domain.creator.adapter.Adapter;

import java.nio.file.Path;
import java.util.List;

/**
 * Adapter definition for automatically removing the background from images.
 */
public interface BackgroundRemovalAdapter extends Adapter<BackgroundRemovalInitParams, List<Path>> {

    /**
     * Removes the background from a single image.
     *
     * @param filePath Path to the image file.
     */
    void removeBackground(Path filePath);

    /**
     * Removes the background from all images of the supplied image set in batch.
     *
     * @param filePaths Paths to the image files.
     */
    void removeBackgrounds(List<Path> filePaths);

}
