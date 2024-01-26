package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.Asset;
import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.MediaCreationService;
import com.arassec.artivact.backend.service.creator.CapturePhotosParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Profile("desktop")
@RequestMapping("/api/media-creation")
public class MediaCreationController extends BaseController {

    private final MediaCreationService mediaCreationService;

    @PostMapping("/{itemId}/capture-photos")
    public ResponseEntity<OperationProgress> capturePhotos(@PathVariable String itemId,
                                                         @RequestBody CapturePhotosParams capturePhotosParams) {
        mediaCreationService.capturePhotos(itemId, capturePhotosParams);
        return getProgress();
    }

    @PostMapping("/{itemId}/remove-backgrounds")
    public ResponseEntity<OperationProgress> capturePhotos(@PathVariable String itemId,
                                                         @RequestParam int imageSetIndex) {
        mediaCreationService.removeBackgrounds(itemId, imageSetIndex);
        return getProgress();
    }

    @PostMapping("/{itemId}/create-image-set")
    public ResponseEntity<OperationProgress> createImageSet(@PathVariable String itemId) {
        mediaCreationService.createImageSetFromDanglingImages(itemId);
        return getProgress();
    }

    @PostMapping("/{itemId}/create-model-set/{pipeline}")
    public ResponseEntity<OperationProgress> createModelSet(@PathVariable String itemId, @PathVariable String pipeline) {
        mediaCreationService.createModel(itemId, pipeline);
        return getProgress();
    }

    @PostMapping("/{itemId}/edit-model/{modelSetIndex}")
    public ResponseEntity<OperationProgress> openModelEditor(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        mediaCreationService.editModel(itemId, modelSetIndex);
        return getProgress();
    }

    @GetMapping("/{itemId}/model-set-files/{modelSetIndex}")
    public ResponseEntity<List<Asset>> getModelSetFiles(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        return ResponseEntity.ok(mediaCreationService.getModelSetFiles(itemId, modelSetIndex));
    }

    @GetMapping("/pipelines")
    public ResponseEntity<List<String>> getPipelines() {
        return ResponseEntity.ok(mediaCreationService.getPipelines());
    }

    @GetMapping("/progress")
    public ResponseEntity<OperationProgress> getProgress() {
        return convert(mediaCreationService.getProgressMonitor());
    }

    @PutMapping("/{itemId}/open-images-dir")
    public ResponseEntity<Void> openImagesDir(@PathVariable String itemId) {
        mediaCreationService.openImagesDir(itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/open-models-dir")
    public ResponseEntity<Void> openModelsDir(@PathVariable String itemId) {
        mediaCreationService.openModelsDir(itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/open-model-dir/{modelSetIndex}")
    public ResponseEntity<Void> openModelDir(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        mediaCreationService.openModelDir(itemId, modelSetIndex);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/transfer-image")
    public ResponseEntity<Void> transferImage(@PathVariable String itemId, @RequestBody Asset image) {
        mediaCreationService.transferImageToMedia(itemId, image);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/transfer-model/{modelSetIndex}")
    public ResponseEntity<Void> transferModel(@PathVariable String itemId, @PathVariable int modelSetIndex, @RequestBody Asset file) {
        mediaCreationService.transferModelToMedia(itemId, modelSetIndex, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}/image-set/{imageSetIndex}")
    public ResponseEntity<Void> deleteImageSet(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        mediaCreationService.deleteImageSet(itemId, imageSetIndex);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}/model-set/{modelSetIndex}")
    public ResponseEntity<Void> deleteModelSet(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        mediaCreationService.deleteModelSet(itemId, modelSetIndex);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{itemId}/image-set/{imageSetIndex}/toggle-model-input")
    public ResponseEntity<Void> toggleModelInput(@PathVariable String itemId, @PathVariable int imageSetIndex) {
        mediaCreationService.toggleModelInput(itemId, imageSetIndex);
        return ResponseEntity.ok().build();
    }

}
