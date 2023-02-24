package com.arassec.artivact.creator.standalone.core.model;

import com.arassec.artivact.common.model.Artivact;
import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class CreatorArtivact extends Artivact {

    private static final String TEMP_DIR = "Temp";

    public static final String IMAGES_PREVIEW_DIR = "preview";

    private static final String MESHROOM_DIR = "Meshroom";

    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

    private static final String MESHROOM_RESULT_DIR = "MeshroomResult";

    private static final String BLENDER_DIR = "Blender";

    private static final String MISC = "Misc";

    private static final String CAMERA_PI_DIR = "CameraPi";

    private static final String FALLBACK_IMAGE = "Utils/fallback-image.png";

    private String id;

    private String notes;

    private List<ArtivactImageSet> imageSets = new LinkedList<>();

    private List<ArtivactModel> models = new LinkedList<>();

    @JsonIgnore
    private Path projectRoot;

    @JsonIgnore
    private FileUtil fileUtil;

    public CreatorArtivact() {
    }

    public CreatorArtivact(String id) {
        this.id = id;
    }

    public Path getImagesPreviewDir(boolean includeProjectRoot) {
        Path imagesDir = getImagesDir(includeProjectRoot);
        return Path.of(imagesDir.toString(), IMAGES_PREVIEW_DIR);
    }

    public Path getProjectTempDir() {
        return projectRoot.resolve(TEMP_DIR);
    }

    public String getPreviewImage() {
        return imageSets.stream()
                .filter(imageSet -> !imageSet.getImages().isEmpty())
                .map(imageSet -> imageSet.getImages().get(0).getPreview())
                .findFirst()
                .orElse(FALLBACK_IMAGE);
    }

    public int getNextAssetNumber(Path assetDir) {
        return getNextAssetNumber(assetDir, List.of(IMAGES_PREVIEW_DIR));
    }

    public ArtivactImage createImage(File asset) {
        log.debug("Creating new image: {}", asset.getPath());
        fileUtil.createDirIfRequired(getImagesPreviewDir(true));

        int nextAssetNumber = getNextAssetNumber(getImagesDir(true));
        String[] assetNameParts = asset.getName().split("\\.");
        var extension = "";
        if (assetNameParts.length > 1) {
            extension = assetNameParts[assetNameParts.length - 1];
        }

        var targetFile = getImagePath(false, nextAssetNumber, extension);
        var targetFileWithProjectRoot = getImagePath(true, nextAssetNumber, extension);

        var previewFile = getImagePreviewPath(false, nextAssetNumber, extension);
        var previewFileWithProjectRoot = getImagePreviewPath(true, nextAssetNumber, extension);

        try {
            Files.copy(Path.of(asset.getPath()), targetFileWithProjectRoot);
            log.debug("Image copied to target dir: {}", targetFileWithProjectRoot);
            writeImagePreview(asset, extension, previewFileWithProjectRoot.toFile());
            log.debug("Image preview created: {}", previewFileWithProjectRoot);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not copy asset!", e);
        }

        return ArtivactImage.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetFile))
                .preview(formatPath(previewFile))
                .build();
    }

    public Path getImagePreviewPath(boolean includeProjectRoot, int assetNumber, String extension) {
        var firstSubDir = getSubDir(0);
        var secondSubDir = getSubDir(1);
        var imageName = getAssetName(assetNumber, extension);
        if (includeProjectRoot) {
            return projectRoot.resolve(Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR,
                    IMAGES_PREVIEW_DIR, imageName));
        }
        return Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR, IMAGES_PREVIEW_DIR, imageName);
    }

    public void deleteImage(ArtivactImage asset) {
        imageSets.forEach(imageSet -> {
            if (imageSet.getImages().contains(asset)) {
                imageSet.getImages().remove(asset);
                try {
                    Files.deleteIfExists(asset.getPreviewPath(projectRoot));
                    Files.deleteIfExists(projectRoot.resolve(Path.of(asset.getPath())));
                } catch (IOException e) {
                    throw new ArtivactCreatorException("Could not delete Images!", e);
                }
            }
        });
    }

    public void deleteImageSet(ArtivactImageSet artivactImageSet) {
        if (getImageSets().contains(artivactImageSet)) {
            List<ArtivactImage> images = new LinkedList<>(artivactImageSet.getImages());
            images.forEach(this::deleteImage);
            imageSets.remove(artivactImageSet);
        }
    }

    public void addModel(Path modelFile) {
        Path modelsDir = getModelsDir(true);

        fileUtil.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(false, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(true, nextAssetNumber);

        fileUtil.createDirIfRequired(targetDirWithProjectRoot);

        var destination = Paths.get(targetDirWithProjectRoot.toString(), modelFile.getFileName().toString());
        try {
            Files.copy(modelFile, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fileUtil.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactCreatorException("Could not copy model files!", e);
        }
        ArtivactModel artivactModel = ArtivactModel.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .preview(FALLBACK_IMAGE)
                .comment("import")
                .exportFiles(new LinkedList<>())
                .build();

        getModels().add(artivactModel);
    }

    public void createModel(Path sourceDir, String comment) {
        Path modelsDir = getModelsDir(true);

        fileUtil.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(false, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(true, nextAssetNumber);

        fileUtil.createDirIfRequired(targetDirWithProjectRoot);

        try (Stream<Path> stream = Files.list(sourceDir)) {
            if (stream.findAny().isEmpty()) {
                fileUtil.deleteDir(targetDirWithProjectRoot);
                throw new ArtivactCreatorException("Source for model creation not present: " + sourceDir);
            }

            try (Stream<Path> sourceStream = Files.walk(sourceDir)) {
                sourceStream.forEach(source -> {
                    var destination = Paths.get(targetDirWithProjectRoot.toString(), source.toString()
                            .substring(sourceDir.toString().length()));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        fileUtil.deleteDir(targetDirWithProjectRoot);
                        throw new ArtivactCreatorException("Could not copy model files!", e);
                    }
                });
            }

        } catch (IOException e) {
            fileUtil.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactCreatorException("Could not copy asset directory!", e);
        }

        ArtivactModel artivactModel = ArtivactModel.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .preview(FALLBACK_IMAGE)
                .comment(comment)
                .exportFiles(new LinkedList<>())
                .build();

        getModels().add(artivactModel);
    }

    public void deleteModel(int index) {
        if (models.size() > index) {
            ArtivactModel modelToDelete = getModels().get(index);
            fileUtil.deleteDir(projectRoot.resolve(modelToDelete.getPath()));
            models.remove(index);
        }
    }

    public void openDirInOs(Path directory) {
        var osString = System.getProperty("os.name");
        String commandString;
        if (!Files.exists(directory)) {
            directory = getMainDir(true);
        }
        if (osString.contains("Windows")) {
            commandString = "cmd /c start " + directory.toAbsolutePath();
        } else {
            commandString = "xdg-open " + directory.toAbsolutePath();
        }
        try {
            Runtime.getRuntime().exec(commandString);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not open directory!", e);
        }
    }

    public void addImages(List<File> images, ProgressMonitor progressMonitor, Boolean backgroundRemoved, boolean modelInput,
                          ArtivactImageSet targetImageSet) {
        if (images != null && !images.isEmpty() && targetImageSet != null) {
            List<ArtivactImage> artivactImages = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                var asset = createImage(image);
                artivactImages.add(asset);
                progressMonitor.updateProgress("(" + index.addAndGet(1) + "/" + images.size() + ")");
            });
            targetImageSet.getImages().addAll(artivactImages);
        }
    }

    public void addImageSet(List<File> images, ProgressMonitor progressMonitor, Boolean backgroundRemoved, boolean modelInput) {
        if (images != null && !images.isEmpty()) {
            List<ArtivactImage> artivactImages = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                var asset = createImage(image);
                artivactImages.add(asset);
                progressMonitor.updateProgress("(" + index.addAndGet(1) + "/" + images.size() + ")");
            });
            imageSets.add(new ArtivactImageSet(modelInput, backgroundRemoved, artivactImages));
        }
    }

    public void deleteArtivactDir(Path rootDir) {
        fileUtil.deleteDir(rootDir.resolve(getMainDir(false)));
        var firstSubDir = rootDir.resolve(DATA_DIR).resolve(getSubDir(0));
        var secondSubDir = Path.of(firstSubDir.toString(), getSubDir(1));
        try {
            try (Stream<Path> stream = Files.list(secondSubDir)) {
                if (stream.findAny().isEmpty()) {
                    fileUtil.deleteDir(secondSubDir);
                }
            }
            try (Stream<Path> stream = Files.list(firstSubDir)) {
                if (stream.findAny().isEmpty()) {
                    fileUtil.deleteDir(firstSubDir);
                }
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not delete all directories for Artivact-ID: " + id);
        }
    }

    private String formatPath(Path path) {
        return path.toString().replace("\\", "/");
    }

    private void writeImagePreview(File image, String extension, File targetFile) throws IOException {
        var targetWidth = 100;
        var targetHeight = 100;

        BufferedImage originalImage = ImageIO.read(image);
        var resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        var type = BufferedImage.TYPE_INT_ARGB;
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            type = BufferedImage.TYPE_INT_RGB;
        }
        var outputImage = new BufferedImage(targetWidth, targetHeight, type);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        ImageIO.write(outputImage, extension, targetFile);
    }

}
