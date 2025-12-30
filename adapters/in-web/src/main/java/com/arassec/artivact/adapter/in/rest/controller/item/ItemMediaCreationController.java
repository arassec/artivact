package com.arassec.artivact.adapter.in.rest.controller.item;

import com.arassec.artivact.adapter.in.rest.controller.BaseController;
import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.domain.model.item.Asset;
import com.arassec.artivact.domain.model.media.CaptureImagesParams;
import com.arassec.artivact.domain.model.media.CreateModelParams;
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

    /**
     * Use case for capture images.
     */
    private final CaptureItemImageUseCase captureImagesUseCase;

    /**
     * Use case for manipulate images.
     */
    private final ManipulateItemImagesUseCase manipulateImagesUseCase;

    /**
     * Use case for manage item images.
     */
    private final ManageItemImagesUseCase manageItemImagesUseCase;

    /**
     * Use case for manage item models.
     */
    private final ManageItemModelsUseCase manageItemModelsUseCase;

    /**
     * Use case for create item model.
     */
    private final CreateItemModelUseCase createItemModelUseCase;

    /**
     * Use case for edit item model.
     */
    private final EditItemModelUseCase editItemModelUseCase;

    /**
     * Captures a single image to directly import into the item's media files.
     *
     * @param itemId              The item's ID.
     * @param captureImagesparams Configuration parameters for image capturing.
     */
    @PostMapping("/capture-image")
    public String captureImage(@PathVariable String itemId,
                               @RequestBody CaptureImagesParams captureImagesparams) {
        return captureImagesUseCase.captureImage(itemId, captureImagesparams);
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
                                  @RequestParam String imageManipulatorPeripheralConfigId,
                                  @RequestParam int imageSetIndex) {
        manipulateImagesUseCase.removeBackgrounds(itemId, imageManipulatorPeripheralConfigId, imageSetIndex);
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
    public void createModelSet(@PathVariable String itemId,
                               @RequestBody CreateModelParams createModelParams) {
        createItemModelUseCase.createModel(itemId, createModelParams);
    }

    /**
     * Starts editing an item's 3D model with an external editor.
     *
     * @param itemId                        The item's ID.
     * @param modelSetIndex                 The model-set index containing the model to edit.
     * @param modelEditorPeripheralConfigId ID of the model editor peripheral to use.
     */
    @PostMapping("/edit-model/{modelSetIndex}")
    public void openModelEditor(@PathVariable String itemId,
                                @PathVariable int modelSetIndex,
                                @RequestParam String modelEditorPeripheralConfigId) {
        editItemModelUseCase.editModel(itemId, modelEditorPeripheralConfigId, modelSetIndex);
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
