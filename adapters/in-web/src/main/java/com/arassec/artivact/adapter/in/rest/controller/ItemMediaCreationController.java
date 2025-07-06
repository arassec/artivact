package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.adapter.in.rest.model.OperationProgress;
import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.application.port.in.operation.GetBackgroundOperationProgressUseCase;
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

    private final GetBackgroundOperationProgressUseCase getBackgroundOperationProgressUseCase;

    private final CaptureItemImagesUseCase captureImagesUseCase;

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
     * @return The progress.
     */
    @PostMapping("/capture-images")
    public ResponseEntity<OperationProgress> captureImages(@PathVariable String itemId,
                                                           @RequestBody CaptureImagesParams captureImagesparams) {
        captureImagesUseCase.capture(itemId, captureImagesparams);
        return getProgress(itemId);
    }

    /**
     * Starts removing backgrounds from images of an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The image set index.
     * @return The progress.
     */
    @PostMapping("/remove-backgrounds")
    public ResponseEntity<OperationProgress> removeBackgrounds(@PathVariable String itemId,
                                                               @RequestParam int imageSetIndex) {
        manipulateImagesUseCase.removeBackgrounds(itemId, imageSetIndex);
        return getProgress(itemId);
    }

    /**
     * Creates a new image-set of an item with images which are currently not associated to the image although laying
     * in the item's images directory.
     *
     * @param itemId The item's ID.
     * @return the progress.
     */
    @PostMapping("/create-image-set")
    public ResponseEntity<OperationProgress> createImageSetFromDanglingImages(@PathVariable String itemId) {
        manageItemImagesUseCase.createImageSetFromDanglingImages(itemId);
        return getProgress(itemId);
    }

    /**
     * Starts 3D model creation for an item.
     *
     * @param itemId The item's ID.
     * @return The progress.
     */
    @PostMapping("/create-model-set")
    public ResponseEntity<OperationProgress> createModelSet(@PathVariable String itemId) {
        createItemModelUseCase.createModel(itemId);
        return getProgress(itemId);
    }

    /**
     * Starts editing an item's 3D model with an external editor.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index containing the model to edit.
     * @return The progress.
     */
    @PostMapping("/edit-model/{modelSetIndex}")
    public ResponseEntity<OperationProgress> openModelEditor(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        editItemModelUseCase.editModel(itemId, modelSetIndex);
        return getProgress(itemId);
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

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @param itemId The item's ID.
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress(@PathVariable String itemId) {
        return convert(getBackgroundOperationProgressUseCase.getProgress());
    }

}
