package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.item.Asset;
import com.arassec.artivact.core.model.item.CreationImageSet;
import com.arassec.artivact.core.model.item.CreationModelSet;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.creator.CapturePhotosParams;
import com.arassec.artivact.domain.creator.ImageCreator;
import com.arassec.artivact.domain.creator.ModelCreator;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Service for item media creation.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MediaCreationService {

    /**
     * Service for item handling.
     */
    private final ItemService itemService;

    /**
     * Creator for item images.
     */
    private final ImageCreator imageCreator;

    /**
     * Creator for item models.
     */
    private final ModelCreator modelCreator;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * Executor service for long-running background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Cleans up the service.
     */
    @PreDestroy
    public void teardown() {
        executorService.shutdownNow();
    }

    /**
     * Captures photos using peripherals like a turntable and camera.
     *
     * @param itemId              The ID of the item to capture photos for.
     * @param capturePhotosParams Parameters for photo capturing.
     */
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

                Item item = itemService.loadTranslated(itemId);
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

    /**
     * Removes the backgrounds from images.
     *
     * @param itemId        The ID of the item.
     * @param imageSetIndex The image-set index containing the images to process.
     */
    public synchronized void removeBackgrounds(String itemId, int imageSetIndex) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "backgroundRemovalStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadTranslated(itemId);

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
                progressMonitor.updateProgress("backgroundRemovalFailed", e);
                log.error("Error during background removal!", e);
            }
        });
    }

    /**
     * Creates a new image-set from images not yet referenced by the item.
     *
     * @param itemId The item's ID.
     */
    public synchronized void createImageSetFromDanglingImages(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "imageSetStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadTranslated(itemId);

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

    /**
     * Creates a new 3D model for the item by starting an external photogrammetry program.
     *
     * @param itemId The item's ID.
     */
    public synchronized void createModel(String itemId) {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "createModelStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadTranslated(itemId);

                List<CreationImageSet> modelInputImageSets = item.getMediaCreationContent().getImageSets().stream()
                        .filter(CreationImageSet::isModelInput)
                        .toList();

                Optional<CreationModelSet> modelSetOptional = modelCreator.createModel(itemId, modelInputImageSets, progressMonitor);

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

    /**
     * Opens the item's model in an external 3D editor.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The model-set index to open the model from.
     */
    public synchronized void editModel(String itemId, int modelSetIndex) {
        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "editModelStart");

        executorService.submit(() -> {
            try {
                Item item = itemService.loadTranslated(itemId);

                CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);

                modelCreator.editModel(progressMonitor, creationModelSet);

                progressMonitor = null;
            } catch (Exception e) {
                progressMonitor.updateProgress("editModelFailed", e);
                log.error("Error during model editing!", e);
            }
        });
    }

    /**
     * Opens the item's images directory with a local file browser.
     *
     * @param itemId The item's ID.
     */
    public void openImagesDir(String itemId) {
        fileRepository.openDirInOs(imageCreator.getImagesDir(itemId, true));
    }

    /**
     * Opens the item's models directory with a local file browser.
     *
     * @param itemId The item's ID.
     */
    public void openModelsDir(String itemId) {
        fileRepository.openDirInOs(modelCreator.getModelsDir(itemId, true));
    }

    /**
     * Opens a specific model directory of an item with a local file browser.
     *
     * @param itemId The item's ID.
     */
    public void openModelDir(String itemId, int modelSetIndex) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileRepository.openDirInOs(projectDataProvider.getProjectRoot().resolve(creationModelSet.getDirectory()));
    }

    /**
     * Returns the {@link Asset}s of an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to load the assets from.
     * @return List of assets in the model-set.
     */
    public List<Asset> getModelSetFiles(String itemId, int modelSetIndex) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        try (Stream<Path> files = Files.list(projectDataProvider.getProjectRoot().resolve(creationModelSet.getDirectory()))) {
            return files
                    .map(file -> Asset.builder()
                            .fileName(file.getFileName().toString())
                            .url(createModelLogoUrl(file.getFileName().toString()))
                            .transferable(file.getFileName().toString().endsWith("glb") || file.getFileName().toString().endsWith("gltf"))
                            .build())
                    .toList();
        } catch (IOException e) {
            log.error("Could not read files of model-set!", e);
            return List.of();
        }
    }

    /**
     * Creates a logo file to display for a given model file.
     *
     * @param filename The filename to determine the logo for.
     * @return The logo filename.
     */
    private String createModelLogoUrl(String filename) {
        String file = filename.toLowerCase();
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

    /**
     * Transfers an image from an item's media-creation section to its media.
     *
     * @param itemId The item's ID.
     * @param image  The image to transfer.
     */
    public void transferImageToMedia(String itemId, Asset image) {
        Path sourcePath = imageCreator.getTransferSourcePath(itemId, image);
        Path targetPath = imageCreator.getTransferTargetPath(itemId, image);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Item item = itemService.loadTranslatedRestricted(itemId);
            item.getMediaContent().getImages().add(targetPath.getFileName().toString());
            itemService.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy image!", e);
        }
    }

    /**
     * Transfers a model from an item's media-creation section to its media.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex Index to the model-set containing the model file.
     * @param model         The model to transfer.
     */
    public void transferModelToMedia(String itemId, int modelSetIndex, Asset model) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);

        Path sourcePath = projectDataProvider.getProjectRoot().resolve(creationModelSet.getDirectory()).resolve(model.getFileName());
        Path targetPath = modelCreator.getTransferTargetPath(itemId, model);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            item.getMediaContent().getModels().add(targetPath.getFileName().toString());
            itemService.save(item);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy model!", e);
        }
    }

    /**
     * Deletes an item's image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index of the image-set to delete.
     */
    public void deleteImageSet(String itemId, int imageSetIndex) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        item.getMediaCreationContent().getImageSets().remove(imageSetIndex);
        itemService.save(item);
    }

    /**
     * Deletes an item's model-set.
     *
     * @param itemId        The item's ID.
     * @param modelSetIndex The index of the model-set to delete.
     */
    public void deleteModelSet(String itemId, int modelSetIndex) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        CreationModelSet creationModelSet = item.getMediaCreationContent().getModelSets().get(modelSetIndex);
        fileRepository.delete(projectDataProvider.getProjectRoot().resolve(Path.of(creationModelSet.getDirectory())).toAbsolutePath());
        item.getMediaCreationContent().getModelSets().remove(modelSetIndex);
        itemService.save(item);
    }

    /**
     * Toggles the "model-input" property of an image-set.
     *
     * @param itemId        The item's ID.
     * @param imageSetIndex The index to the image-set that should be modified.
     */
    public void toggleModelInput(String itemId, int imageSetIndex) {
        Item item = itemService.loadTranslatedRestricted(itemId);
        item.getMediaCreationContent().getImageSets().get(imageSetIndex).setModelInput(
                !item.getMediaCreationContent().getImageSets().get(imageSetIndex).isModelInput()
        );
        itemService.save(item);
    }

}
