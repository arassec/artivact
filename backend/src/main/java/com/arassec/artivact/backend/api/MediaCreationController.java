package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.Asset;
import com.arassec.artivact.backend.api.model.OperationProgress;
import com.arassec.artivact.backend.service.MediaCreationService;
import com.arassec.artivact.backend.service.creator.CapturePhotosParams;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
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
public class MediaCreationController {

    private final MediaCreationService mediaCreationService;

    @PostMapping("/{itemId}/capture-photos")
    public ResponseEntity<ProgressMonitor> capturePhotos(@PathVariable String itemId,
                                                         @RequestBody CapturePhotosParams capturePhotosParams) {
        return ResponseEntity.ok(mediaCreationService.capturePhotos(itemId, capturePhotosParams));
    }

    @PostMapping("/{itemId}/remove-backgrounds")
    public ResponseEntity<ProgressMonitor> capturePhotos(@PathVariable String itemId,
                                                         @RequestParam int imageSetIndex) {
        return ResponseEntity.ok(mediaCreationService.removeBackgrounds(itemId, imageSetIndex));
    }

    @PostMapping("/{itemId}/create-image-set")
    public ResponseEntity<ProgressMonitor> createImageSet(@PathVariable String itemId) {
        return ResponseEntity.ok(mediaCreationService.createImageSetFromDanglingImages(itemId));
    }

    @PostMapping("/{itemId}/create-model-set/{pipeline}")
    public ResponseEntity<ProgressMonitor> createModelSet(@PathVariable String itemId, @PathVariable String pipeline) {
        return ResponseEntity.ok(mediaCreationService.createModel(itemId, pipeline));
    }

    @PostMapping("/{itemId}/edit-model/{modelSetIndex}")
    public ResponseEntity<ProgressMonitor> openModelEditor(@PathVariable String itemId, @PathVariable int modelSetIndex) {
        return ResponseEntity.ok(mediaCreationService.editModel(itemId, modelSetIndex));
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
        ProgressMonitor progressMonitor = mediaCreationService.getProgressMonitor();
        if (progressMonitor != null) {
            return ResponseEntity.ok(new OperationProgress(progressMonitor.getProgress()));
        }
        return ResponseEntity.ok().build();
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
