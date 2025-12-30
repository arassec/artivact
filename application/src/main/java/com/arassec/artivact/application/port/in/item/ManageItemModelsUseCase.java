package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.item.Asset;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * Use case for manage item models operations.
 */
public interface ManageItemModelsUseCase {

    /**
     * Loads an item's model.
     *
     * @param itemId   The ID of the item.
     * @param filename The model's filename.
     * @return The model as {@link FileSystemResource}.
     */
    byte[] loadModel(String itemId, String filename);

    /**
     * Saves a model to an item.
     *
     * @param itemId          The ID of the item.
     * @param filename        The name of the model file.
     * @param data            The model's data as {@link InputStream}.
     * @param keepAssetNumber Set to {@code true}, if the asset number from the model's filename should be used.
     */
    void saveModel(String itemId, String filename, InputStream data, boolean keepAssetNumber);

    /**
     * Adds a model to the item.
     *
     * @param itemId ID of the item.
     * @param file   The model file to add.
     */
    void addModel(String itemId, MultipartFile file);

    /**
     * Opens the item's models directory with a local file browser.
     *
     * @param itemId The item's ID.
     */
    void openModelsDir(String itemId);

    /**
     * Opens a specific model directory of an item with a local file browser.
     *
     * @param itemId The item's ID.
     */
    void openModelDir(String itemId, int modelSetIndex);

    /**
     * Returns the {@link Asset}s of an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to load the assets from.
     * @return List of assets in the model-set.
     */
    List<Asset> getModelSetFiles(String itemId, int modelSetIndex);

    /**
     * Checks whether a transferable model from an item's media-creation section exists or not.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex Index to the model-set containing the model file.
     * @return {@code true} if a transferable model exists, {@code false} otherwise.
     */
    boolean hasTransferableModel(String itemId, int modelSetIndex);

    /**
     * Transfers a model from an item's media-creation section to its media.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex Index to the model-set containing the model file.
     */
    void transferModelToMedia(String itemId, int modelSetIndex);

    /**
     * Deletes an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to delete.
     */
    void deleteModelSet(String itemId, int modelSetIndex);

    /**
     * Toggles the "model-input" property of an image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index to the image-set that should be modified.
     */
    void toggleModelInput(String itemId, int imageSetIndex);

}
