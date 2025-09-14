package com.arassec.artivact.adapter.in.rest.controller.item;

import com.arassec.artivact.adapter.in.rest.controller.BaseController;
import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller for item media-creation.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Profile({"desktop", "e2e"})
@RequestMapping("/api/item/{itemId}/media-creation")
public class ItemMediaCreationController extends BaseController {

    private final CaptureItemImageUseCase captureImagesUseCase;

    private final ManipulateItemImagesUseCase manipulateImagesUseCase;

    private final ManageItemImagesUseCase manageItemImagesUseCase;

    private final ManageItemModelsUseCase manageItemModelsUseCase;

    private final CreateItemModelUseCase createItemModelUseCase;

    private final EditItemModelUseCase editItemModelUseCase;

    /**
     * Starts capturing images into a new image-set of an item.
     *
     * @param itemId              The item's ID.
     * @param captureImagesparams Configuration parameters for image capturing.
     */
    @PostMapping("/capture-image")
    public String captureImage(@PathVariable String itemId,
                               @RequestBody CaptureImagesParams captureImagesparams) {
        return captureImagesUseCase.captureImage(itemId, captureImagesparams.isRemoveBackgrounds());
    }

    /**
     * Starts capturing images into a new image-set of an item.
     *
     * @param itemId              The item's ID.
     * @param captureImagesparams Configuration parameters for image capturing.
     */
    @PostMapping("/capture-images")
    public void captureImages(@PathVariable String itemId,
                              @RequestBody CaptureImagesParams captureImagesparams) {
        captureImagesUseCase.captureImages(itemId, captureImagesparams);
    }

    /**
     * Starts removing backgrounds from images of an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The image set index.
     */
    @PostMapping("/remove-backgrounds")
    public void removeBackgrounds(@PathVariable String itemId,
                                  @RequestParam int imageSetIndex) {
        manipulateImagesUseCase.removeBackgrounds(itemId, imageSetIndex);
    }

    /**
     * Creates a new image-set of an item with images which are currently not associated to the image although laying
     * in the item's images directory.
     *
     * @param itemId The item's ID.
     */
    @PostMapping("/create-image-set")
    public void createImageSetFromDanglingImages(@PathVariable String itemId) {
        manageItemImagesUseCase.createImageSetFromDanglingImages(itemId);
    }

    /**
     * Starts 3D model creation for an item.
     *
     * @param itemId The item's ID.
     */
    @PostMapping("/create-model-set")
    public void createModelSet(@PathVariable String itemId) {
        createItemModelUseCase.createModel(itemId);
    }

    /**
     * Starts editing an item's 3D model with an external editor.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index containing the model to edit.
     */
    @PostMapping("/edit-model/{modelSetIndex}")
    public void openModelEditor(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        editItemModelUseCase.editModel(itemId, modelSetIndex);
    }

    /**
     * Returns the assets of an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index to get the files from.
     * @return The list of assets of the model-set.
     */
    @GetMapping("/model-set-files/{modelSetIndex}")
    public ResponseEntity<List<Asset>> getModelSetFiles(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        return ResponseEntity.ok(manageItemModelsUseCase.getModelSetFiles(itemId, modelSetIndex));
    }

    /**
     * Opens an item's images directory with the system's file browser.
     *
     * @param itemId The item's ID.
     */
    @PutMapping("/open-images-dir")
    public void openImagesDir(@PathVariable String itemId) {
        manageItemImagesUseCase.openImagesDir(itemId);
    }

    /**
     * Opens an item's models directory with the system's file browser.
     *
     * @param itemId The item's ID.
     */
    @PutMapping("/open-models-dir")
    public void openModelsDir(@PathVariable String itemId) {
        manageItemModelsUseCase.openModelsDir(itemId);
    }

    /**
     * Opens an item's specific model directory with the system's file browser.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to open in the file browser.
     */
    @PutMapping("/open-model-dir/{modelSetIndex}")
    public void openModelDir(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        manageItemModelsUseCase.openModelDir(itemId, modelSetIndex);
    }

    /**
     * Transfers an item's image from media-creation to media.
     *
     * @param itemId The item's ID.
     * @param image  The image to transfer.
     */
    @PutMapping("/transfer-image")
    public void transferImage(@PathVariable String itemId, @RequestBody Asset image) {
        manageItemImagesUseCase.transferImageToMedia(itemId, image);
    }

    /**
     * Transfers an item's model from media-creation to media.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set containing the model to transfer.
     * @param file          The model file to transfer.
     */
    @PutMapping("/transfer-model/{modelSetIndex}")
    public void transferModel(@PathVariable String itemId, @PathVariable int modelSetIndex, @RequestBody Asset file) {
        manageItemModelsUseCase.transferModelToMedia(itemId, modelSetIndex, file);
    }

    /**
     * Deletes an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to delete.
     */
    @DeleteMapping("/image-set/{imageSetIndex}")
    public void deleteImageSet(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        manageItemImagesUseCase.deleteImageSet(itemId, imageSetIndex);
    }

    /**
     * Deletes an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to delete.
     */
    @DeleteMapping("/model-set/{modelSetIndex}")
    public void deleteModelSet(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        manageItemModelsUseCase.deleteModelSet(itemId, modelSetIndex);
    }

    /**
     * Toggles whether to ues an item's image-set as model input or not.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to configure.
     */
    @PutMapping("/image-set/{imageSetIndex}/toggle-model-input")
    public void toggleModelInput(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        manageItemModelsUseCase.toggleModelInput(itemId, imageSetIndex);
    }

}
