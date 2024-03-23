package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.model.item.Asset;
import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.MediaCreationService;
import com.arassec.artivact.backend.service.creator.CapturePhotosParams;
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
@Profile("desktop")
@RequestMapping("/api/item/{itemId}/media-creation")
public class ItemMediaCreationController extends BaseController {

    /**
     * The application's {@link MediaCreationService}.
     */
    private final MediaCreationService mediaCreationService;

    /**
     * Starts capturing photos into a new image-set of an item.
     *
     * @param itemId              The item's ID.
     * @param capturePhotosParams Configuration parameters for photo capturing.
     * @return The progress.
     */
    @PostMapping("/capture-photos")
    public ResponseEntity<OperationProgress> capturePhotos(@PathVariable String itemId,
                                                           @RequestBody CapturePhotosParams capturePhotosParams) {
        mediaCreationService.capturePhotos(itemId, capturePhotosParams);
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
    public ResponseEntity<OperationProgress> capturePhotos(@PathVariable String itemId,
                                                           @RequestParam int imageSetIndex) {
        mediaCreationService.removeBackgrounds(itemId, imageSetIndex);
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
    public ResponseEntity<OperationProgress> createImageSet(@PathVariable String itemId) {
        mediaCreationService.createImageSetFromDanglingImages(itemId);
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
        mediaCreationService.createModel(itemId);
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
        mediaCreationService.editModel(itemId, modelSetIndex);
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
        return ResponseEntity.ok(mediaCreationService.getModelSetFiles(itemId, modelSetIndex));
    }

    /**
     * Opens an item's images directory with the system's file browser.
     *
     * @param itemId The item's ID.
     */
    @PutMapping("/open-images-dir")
    public void openImagesDir(@PathVariable String itemId) {
        mediaCreationService.openImagesDir(itemId);
    }

    /**
     * Opens an item's models directory with the system's file browser.
     *
     * @param itemId The item's ID.
     */
    @PutMapping("/open-models-dir")
    public void openModelsDir(@PathVariable String itemId) {
        mediaCreationService.openModelsDir(itemId);
    }

    /**
     * Opens an item's specific model directory with the system's file browser.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to open in the file browser.
     */
    @PutMapping("/open-model-dir/{modelSetIndex}")
    public void openModelDir(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        mediaCreationService.openModelDir(itemId, modelSetIndex);
    }

    /**
     * Transfers an item's image from media-creation to media.
     *
     * @param itemId The item's ID.
     * @param image  The image to transfer.
     */
    @PutMapping("/transfer-image")
    public void transferImage(@PathVariable String itemId, @RequestBody Asset image) {
        mediaCreationService.transferImageToMedia(itemId, image);
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
        mediaCreationService.transferModelToMedia(itemId, modelSetIndex, file);
    }

    /**
     * Deletes an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to delete.
     */
    @DeleteMapping("/image-set/{imageSetIndex}")
    public void deleteImageSet(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        mediaCreationService.deleteImageSet(itemId, imageSetIndex);
    }

    /**
     * Deletes an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to delete.
     */
    @DeleteMapping("/model-set/{modelSetIndex}")
    public void deleteModelSet(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        mediaCreationService.deleteModelSet(itemId, modelSetIndex);
    }

    /**
     * Toggles whether to ues an item's image-set as model input or not.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to configure.
     */
    @PutMapping("/image-set/{imageSetIndex}/toggle-model-input")
    public void toggleModelInput(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        mediaCreationService.toggleModelInput(itemId, imageSetIndex);
    }

    /**
     * Returns the progress of a previously started long-running operation.
     *
     * @param itemId The item's ID.
     * @return The progress.
     */
    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress(@PathVariable String itemId) {
        log.debug("Checking media-creation progress for item: {}", itemId);
        return convert(mediaCreationService.getProgressMonitor());
    }

}
