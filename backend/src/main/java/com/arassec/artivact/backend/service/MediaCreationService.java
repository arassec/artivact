package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.api.model.Asset;
import com.arassec.artivact.backend.service.creator.CapturePhotosParams;
import com.arassec.artivact.backend.service.creator.ImageCreator;
import com.arassec.artivact.backend.service.creator.ModelCreator;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.CreationImageSet;
import com.arassec.artivact.backend.service.model.item.CreationModelSet;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MediaCreationService {

    private final ItemService itemService;

    private final ImageCreator imageCreator;

    private final ModelCreator modelCreator;

    private final FileUtil fileUtil;

    private final ProjectRootProvider projectRootProvider;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Getter
    private ProgressMonitor progressMonitor;

    @PreDestroy
    public void teardown() {
        executorService.shutdownNow();
    }

    public synchronized void capturePhotos(String itemId, CapturePhotosParams capturePhotosParams) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "captureStart");

        executorService.submit(() -> {
            try {
                List<CreationImageSet> creationImageSets = imageCreator.capturePhotos(itemId,
                        capturePhotosParams.getNumPhotos(),
                        capturePhotosParams.isUseTurnTable(),
                        capturePhotosParams.getTurnTableDelay(),
                        capturePhotosParams.isRemoveBackgrounds(),
                        progressMonitor);

                Item item = itemService.loadUnrestricted(itemId);
                item.getMediaCreationContent().getImageSets().addAll(creationImageSets);
                itemService.save(item);

                // Sleep some time to avoid frontend update without recognizing the new image set(s)...
                TimeUnit.MILLISECONDS.sleep(500);

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("captureFailed", e);
                log.error("Error during photo capturing!", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    public synchronized void removeBackgrounds(String itemId, int imageSetIndex) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "rembgStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadUnrestricted(itemId);

                List<Path> imagesWithoutBackground = imageCreator
                        .removeBackgrounds(itemId, item.getMediaCreationContent().getImageSets().get(imageSetIndex), progressMonitor);

                if (!imagesWithoutBackground.isEmpty()) {
                    item.getMediaCreationContent().getImageSets().add(CreationImageSet.builder()
                            .backgroundRemoved(true)
                            .modelInput(true)
                            .files(imagesWithoutBackground.stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .toList())
                            .build());

                    itemService.save(item);
                }

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("rembgFailed", e);
                log.error("Error during background removal!", e);
            }
        });
    }

    public synchronized void createImageSetFromDanglingImages(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "imageSetStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadUnrestricted(itemId);

                List<String> newImages = itemService.getDanglingImages(item);

                if (!newImages.isEmpty()) {
                    item.getMediaCreationContent().getImageSets().add(CreationImageSet.builder()
                            .backgroundRemoved(null)
                            .modelInput(true)
                            .files(newImages)
                            .build());

                    itemService.save(item);
                }

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("imageSetFailed", e);
                log.error("Error during image-set creation!", e);
            }
        });
    }

    public synchronized void createModel(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "createModelStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadUnrestricted(itemId);

                List<CreationImageSet> modelInputImageSets = item.getMediaCreationContent().getImageSets().stream()
                        .filter(CreationImageSet::isModelInput)
                        .toList();

                Optional<CreationModelSet> modelSetOptional = modelCreator.createModel(itemId, modelInputImageSets, modelCreator.getDefaultPipeline(), progressMonitor);

                if (modelSetOptional.isPresent()) {
                    item.getMediaCreationContent().getModelSets().add(modelSetOptional.get());
                    itemService.save(item);
                }

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("createModelFailed", e);
                log.error("Error during model creation!", e);
            }
        });
    }

    public synchronized void editModel(String itemId, int modelSetIndex) {
        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "editModelStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadUnrestricted(itemId);

                CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);

                modelCreator.editModel(progressMonitor, creationModelSet);

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("editModelFailed", e);
                log.error("Error during model editing!", e);
            }
        });
    }

    public List<String> getPipelines() {
        return modelCreator.getPipelines();
    }

    public void openImagesDir(String itemId) {
        fileUtil.openDirInOs(imageCreator.getImagesDir(itemId, true));
    }

    public void openModelsDir(String itemId) {
        fileUtil.openDirInOs(modelCreator.getModelsDir(itemId, true));
    }

    public void openModelDir(String itemId, int modelSetIndex) {
        Item item = itemService.load(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileUtil.openDirInOs(projectRootProvider.getProjectRoot().resolve(creationModelSet.getDirectory()));
    }

    public List<Asset> getModelSetFiles(String itemId, int modelSetIndex) {
        Item item = itemService.load(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        try (Stream<Path> files = Files.list(projectRootProvider.getProjectRoot().resolve(creationModelSet.getDirectory()))) {
            return files
                    .map(file -> Asset.builder()
                            .fileName(file.getFileName().toString())
                            .url(createModelFileUrl(file.getFileName().toString()))
                            .transferable(file.getFileName().toString().endsWith("glb") || file.getFileName().toString().endsWith("gltf"))
                            .build())
                    .toList();
        } catch (IOException e) {
            log.error("Could not read files of model-set!", e);
            return List.of();
        }
    }

    private String createModelFileUrl(String fileName) {
        String file = fileName.toLowerCase();
        if (file.endsWith("glb") || file.endsWith("gltf")) {
            return "gltf-logo.png";
        } else if (file.endsWith("blend") || file.endsWith("blend1")) {
            return "blender-logo.png";
        } else if (file.endsWith("obj") || file.endsWith("mtl")) {
            return "obj-logo.png";
        } else if (file.endsWith("jpg") || file.endsWith("jpeg") || file.endsWith("png")) {
            return "image-file-logo.png";
        }
        return "unknown-file-logo.png";
    }

    public void transferImageToMedia(String itemId, Asset image) {
        Path sourcePath = imageCreator.getTransferSourcePath(itemId, image);
        Path targetPath = imageCreator.getTransferTargetPath(itemId, image);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Item item = itemService.load(itemId);
            item.getMediaContent().getImages().add(targetPath.getFileName().toString());
            itemService.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy image!", e);
        }
    }

    public void transferModelToMedia(String itemId, int modelSetIndex, Asset file) {
        Item item = itemService.load(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);

        Path sourcePath = projectRootProvider.getProjectRoot().resolve(creationModelSet.getDirectory()).resolve(file.getFileName());
        Path targetPath = modelCreator.getTransferTargetPath(itemId, file);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            item.getMediaContent().getModels().add(targetPath.getFileName().toString());
            itemService.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy model!", e);
        }
    }

    public void deleteImageSet(String itemId, int imageSetIndex) {
        Item item = itemService.load(itemId);
        item.getMediaCreationContent().getImageSets().remove(imageSetIndex);
        itemService.save(item);
    }

    public void deleteModelSet(String itemId, int modelSetIndex) {
        Item item = itemService.load(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileUtil.deleteDir(projectRootProvider.getProjectRoot().resolve(Path.of(creationModelSet.getDirectory())).toAbsolutePath());
        item.getMediaCreationContent().getModelSets().remove(modelSetIndex);
        itemService.save(item);
    }

    public void toggleModelInput(String itemId, int imageSetIndex) {
        Item item = itemService.load(itemId);
        item.getMediaCreationContent().getImageSets().get(imageSetIndex).setModelInput(
                !item.getMediaCreationContent().getImageSets().get(imageSetIndex).isModelInput()
        );
        itemService.save(item);
    }

}
