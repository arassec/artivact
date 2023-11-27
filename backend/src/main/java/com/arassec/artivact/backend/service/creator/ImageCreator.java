package com.arassec.artivact.backend.service.creator;

import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.creator.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.background.BackgroundRemovalInitParams;
import com.arassec.artivact.backend.service.creator.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.camera.CameraInitParams;
import com.arassec.artivact.backend.service.creator.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.turntable.TurntableInitParams;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.ProjectDir;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.model.item.asset.Image;
import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import com.arassec.artivact.backend.service.util.DirectoryWatcher;
import com.arassec.artivact.backend.service.util.FileUtil;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for image handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCreator extends BaseCreator {

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * Provides the project's root directory.
     */
    private final ProjectRootProvider projectRootProvider;

    /**
     * The file util.
     */
    private final FileUtil fileUtil;

    /**
     * Message source for I18N.
     */
    private final MessageSource messageSource;

    /**
     * Watches for new images in the filesystem during photo capturing.
     */
    private final DirectoryWatcher directoryWatcher;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * List of all available adapters.
     */
    @Getter
    private final List<Adapter<?, ?>> adapters;

    /**
     * Captures photos using adapters and adds them to a new image set of the currently active item.
     *
     * @param itemId            The ID of the item to capture photos for.
     * @param numPhotos         The number of photos to capture.
     * @param useTurnTable      {@code true} if a turntable should be used.
     * @param turnTableDelay    Delay of the turntable communication.
     * @param removeBackgrounds Automatically removes backgrounds from newly captured images when set to {@code true}.
     * @param progressMonitor   The progress monitor which is updated during processing.
     * @return The {@link ImageSet} containing the images.
     */
    public List<ImageSet> capturePhotos(String itemId, int numPhotos, boolean useTurnTable,
                                  int turnTableDelay, boolean removeBackgrounds,
                                  ProgressMonitor progressMonitor) {

        List<ImageSet> result = new LinkedList<>();
        ImageSet captureResult = new ImageSet();

        Path targetDir = getImagesDir(itemId, true);

        progressMonitor.setProgressPrefix(messageSource.getMessage("image-service.capture-photos.progress.prefix", null, Locale.getDefault()));

        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();

        // Initialize adapters:
        log.debug("Initializing turntable adapter for photo capturing.");
        TurntableAdapter turntableAdapter = getTurntableAdapter(adapterConfiguration);
        turntableAdapter.initialize(progressMonitor, TurntableInitParams.builder()
                .turntableDelay(turnTableDelay)
                .build());

        log.debug("Initializing camera adapter for photo capturing.");
        CameraAdapter cameraAdapter = getCameraAdapter(adapterConfiguration);
        cameraAdapter.initialize(progressMonitor, CameraInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(targetDir)
                .build());

        log.debug("Initializing background-removal adapter for photo capturing.");
        BackgroundRemovalAdapter backgroundRemovalAdapter = getBackgroundRemovalAdapter(adapterConfiguration);
        backgroundRemovalAdapter.initialize(progressMonitor, BackgroundRemovalInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

        log.debug("Starting capturing of images.");

        // Prepare directory watcher to detect newly captured photos:
        List<Path> capturedImages = Collections.synchronizedList(new LinkedList<>());
        directoryWatcher.startWatching(targetDir, numPhotos,
                newImage -> processNewImage(removeBackgrounds, backgroundRemovalAdapter, newImage, capturedImages));

        // Start capturing:
        for (var i = 0; i < numPhotos; i++) {
            log.debug("Capturing image: {}", i);
            if (progressMonitor.isCancelled()) {
                return result;
            }
            progressMonitor.setProgress("(" + (i + 1) + "/" + numPhotos + ")");
            String filename = getAssetName(getNextAssetNumber(targetDir), null);
            cameraAdapter.captureImage(filename);
            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos);
            }
        }

        // Finished capturing photos. Wait for all images to be detected and stop watching for new files.
        directoryWatcher.finishWatching(2500);

        // Add new images to active item:
        addCapturedImagesToImageSet(itemId, capturedImages, progressMonitor, captureResult);
        result.add(captureResult);

        // Teardown adapters:
        turntableAdapter.teardown();
        cameraAdapter.teardown();
        Optional<List<Path>> imagesWithoutBackground = backgroundRemovalAdapter.teardown();
        if (removeBackgrounds && imagesWithoutBackground.isPresent()) {
            ImageSet bgRemovalResult = new ImageSet();
            addCapturedImagesToImageSet(itemId, capturedImages, progressMonitor, bgRemovalResult);
            result.add(bgRemovalResult);
        }

        return result;
    }

    /**
     * Removes backgrounds from all images in the image set with the given index.
     *
     * @param itemId          The ID of the item to which the images belong.
     * @param imageSet        The image-set to process images from.
     * @param progressMonitor The progress monitor which is updated during processing.
     * @return List of paths of newly created images without background.
     */
    public List<Path> removeBackgrounds(String itemId, ImageSet imageSet, ProgressMonitor progressMonitor) {
        AdapterConfiguration adapterConfiguration = configurationService.loadAdapterConfiguration();
        BackgroundRemovalAdapter backgroundRemovalAdapter = getBackgroundRemovalAdapter(adapterConfiguration);

        backgroundRemovalAdapter.initialize(progressMonitor, BackgroundRemovalInitParams.builder()
                .adapterConfiguration(adapterConfiguration)
                .targetDir(getImagesDir(itemId, true))
                .build());

        backgroundRemovalAdapter.removeBackgrounds(imageSet);

        return backgroundRemovalAdapter.teardown().orElseGet(List::of);
    }

    /**
     * Creates a new image set for the currently active item from external added images.
     *
     * @param images            List of image files to add to the set.
     * @param progressMonitor   The progress monitor which is updated during processing.
     * @param backgroundRemoved Set to {@code true} if the background of the images is already removed.
     * @param modelInput        Set to {@code true} if the images should be used for model creation.
     * @return An {@link ImageSet} with the provided images.
     */
    public Optional<ImageSet> createImageSet(String itemId, List<File> images, ProgressMonitor progressMonitor, Boolean backgroundRemoved, boolean modelInput) {
        if (images != null && !images.isEmpty()) {
            List<Image> artivactImages = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                log.debug("Creating new image: {}", image.getPath());

                int nextAssetNumber = getNextAssetNumber(getImagesDir(itemId, true));
                String[] assetNameParts = image.getName().split("\\.");
                var extension = "";
                if (assetNameParts.length > 1) {
                    extension = assetNameParts[assetNameParts.length - 1];
                }

                var targetFile = getImagePath(itemId, false, nextAssetNumber, extension);
                var targetFileWithProjectRoot = getImagePath(itemId, true, nextAssetNumber, extension);

                try {
                    Files.copy(Path.of(image.getPath()), targetFileWithProjectRoot);
                    log.debug("Image copied to target dir: {}", targetFileWithProjectRoot);
                } catch (IOException e) {
                    throw new ArtivactException("Could not copy asset!", e);
                }

                var asset = Image.builder()
                        .number(nextAssetNumber)
                        .path(formatPath(targetFile))
                        .build();

                artivactImages.add(asset);
                progressMonitor.updateProgress("(" + index.addAndGet(1) + "/" + images.size() + ")");
            });

            return Optional.of(new ImageSet(modelInput, backgroundRemoved, artivactImages));
        }

        return Optional.empty();
    }

    /**
     * Deletes the provided image from any image set of the currently active item and the filesystem.
     *
     * @param image The image to delete.
     */
    public void deleteImage(Image image) {
        try {
            Files.deleteIfExists(projectRootProvider.getProjectRoot().resolve(Path.of(image.getPath())));
        } catch (IOException e) {
            throw new ArtivactException("Could not delete Images!", e);
        }
    }

    /**
     * Deletes the provided image set from the currently active item.
     *
     * @param imageSet The image set to delete.
     */
    public void deleteImageSetFiles(ImageSet imageSet) {
        List<Image> images = new LinkedList<>(imageSet.getImages());
        images.forEach(this::deleteImage);
    }

    /**
     * Returns the images directory of the currently active item.
     *
     * @param itemId             The ID of the item to get the images directory for.
     * @param includeProjectRoot Set to {@code true} to append the images directory path to the project's root directory.
     * @return The path to the currently active item's images directory.
     */
    public Path getImagesDir(String itemId, boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(itemId, projectRootProvider.getProjectRoot(), ProjectDir.IMAGES_DIR);
        }
        return getAssetDir(itemId, null, ProjectDir.IMAGES_DIR);
    }

    /**
     * Adds internally captured images to an existing image set of the currently active item.
     *
     * @param images          The images to add to the set.
     * @param progressMonitor The progress monitor which is updated during processing.
     * @param targetImageSet  The target image set to add the images to.
     */
    private void addCapturedImagesToImageSet(String itemId, List<Path> images, ProgressMonitor progressMonitor, ImageSet targetImageSet) {
        if (images != null && !images.isEmpty() && targetImageSet != null) {
            List<Image> artivactImages = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                var addedImage = addImage(itemId, image);
                artivactImages.add(addedImage);
                progressMonitor.updateProgress("(" + index.addAndGet(1) + "/" + images.size() + ")");
            });
            targetImageSet.getImages().addAll(artivactImages);
        }
    }

    /**
     * Processes a newly created image file. Other than "JPEG" files are ignored.
     *
     * @param removeBackgrounds        Set to {@code true} to directly remove backgrounds of newly found images.
     * @param backgroundRemovalAdapter The background removal adapter to use.
     * @param createdFile              The newly found file.
     * @param capturedImages           Target list where newly processed images will be stored in.
     */
    private void processNewImage(boolean removeBackgrounds, BackgroundRemovalAdapter backgroundRemovalAdapter,
                                 Path createdFile, List<Path> capturedImages) {
        if (createdFile.toString().toLowerCase().endsWith(".jpg") || createdFile.toString().toLowerCase().endsWith(".jpeg")) {
            int iterations = 100;
            int i = 0;
            while (!Files.exists(createdFile) && i < iterations) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ArtivactException("Interrupted during processing of newly created image file!", e);
                }
                i++;
            }
            log.debug("Captured image file found: {}", createdFile);

            capturedImages.add(createdFile);

            if (removeBackgrounds) {
                backgroundRemovalAdapter.removeBackground(createdFile);
            }
        }
    }

    /**
     * Returns the path to an image with the given asset number.
     *
     * @param itemId             ID of the item to get the image path from.
     * @param includeProjectRoot Set to {@code true} to append the image path to the project's root directory.
     * @param assetNumber        The image's asset number.
     * @param extension          The image's file extension.
     * @return Path to the image file.
     */
    private Path getImagePath(String itemId, boolean includeProjectRoot, int assetNumber, String extension) {
        var firstSubDir = getSubDir(itemId, 0);
        var secondSubDir = getSubDir(itemId, 1);
        var imageName = getAssetName(assetNumber, extension);
        Path resultPath = Path.of(ProjectDir.ITEMS_DIR, firstSubDir, secondSubDir, itemId, ProjectDir.IMAGES_DIR, imageName);
        if (includeProjectRoot) {
            return projectRootProvider.getProjectRoot().resolve(resultPath);
        }
        return resultPath;
    }

    /**
     * Adds an image to the provided item.
     *
     * @param itemId The ID of the item to add the image to.
     * @param image  The image to add.
     * @return The newly created {@link Image}.
     */
    private Image addImage(String itemId, Path image) {
        log.debug("Adding new image: {}", image);

        String[] assetNameParts = image.getFileName().toString().split("\\.");
        int assetNumber = Integer.parseInt(assetNameParts[0]);
        var extension = "";
        if (assetNameParts.length > 1) {
            extension = assetNameParts[assetNameParts.length - 1];
        }

        var targetFile = getImagePath(itemId, false, assetNumber, extension);

        return Image.builder()
                .number(assetNumber)
                .path(formatPath(targetFile))
                .build();
    }

}
