package com.arassec.artivact.application.port.out.adapter;

import com.arassec.artivact.domain.model.adapter.PeripheralAdapter;

import java.nio.file.Path;
import java.util.List;

public interface ImageManipulationAdapter extends PeripheralAdapter {

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

    /**
     * Returns the list of modified images.
     *
     * @return The list with {@link Path}s to the modified image files.
     */
    List<Path> getModifiedImages();

}
