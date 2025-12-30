package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.item.ImageSize;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Use case for manage item images operations.
 */
public interface ManageItemImagesUseCase {

    /**
     * Loads an item's image.
     *
     * @param itemId     The ID of the item.
     * @param filename   The filename of the image.
     * @param targetSize The desired image target size.
     * @return The (scaled) image as {@link FileSystemResource}.
     */
    byte[] loadImage(String itemId, String filename, ImageSize targetSize);

    /**
     * Saves an image to an item.
     *
     * @param itemId          The ID of the item.
     * @param filename        The name of the image file.
     * @param data            The image's data as {@link InputStream}.
     * @param keepAssetNumber Set to {@code true}, if the asset number from the image's filename should be used.
     */
    void saveImage(String itemId, String filename, InputStream data, boolean keepAssetNumber);

    /**
     * Adds an image to the item.
     *
     * @param itemId ID of the item.
     * @param file   The image file to add.
     */
    void addImage(String itemId, MultipartFile file);

    /**
     * Creates a new image-set from images not yet referenced by the item.
     *
     * @param itemId The item's ID.
     */
    void createImageSetFromDanglingImages(String itemId);

    /**
     * Opens the item's images directory with a local file browser.
     *
     * @param itemId The item's ID.
     */
    void openImagesDir(String itemId);

    /**
     * Transfers an image from an item's media-creation section to its media.
     *
     * @param itemId The item's ID.
     * @param image  The image to transfer.
     */
    void transferImageToMedia(String itemId, Asset image);

    /**
     * Deletes an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to delete.
     */
    void deleteImageSet(String itemId, int imageSetIndex);

}
