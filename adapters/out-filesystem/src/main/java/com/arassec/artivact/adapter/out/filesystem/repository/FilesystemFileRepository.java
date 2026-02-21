package com.arassec.artivact.adapter.out.filesystem.repository;

import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.imgscalr.Scalr;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link FileRepository} implementation that uses the operating system's filesystem.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FilesystemFileRepository implements FileRepository {

    /**
     * Error message if image scaling fails.
     */
    private static final String COULD_NOT_SCALE_IMAGE = "Could not scale image!";

    /**
     * Error message if copying a resource fails.
     */
    private static final String COULD_NOT_COPY_RESOURCE = "Could not copy resource!";

    /**
     * Spring's {@link Environment}.
     */
    private final Environment environment;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProjectDirectory(Path projectRoot, Path projectSetupDir, Path projectSetupDirFallback) {

        if (!environment.matchesProfiles("desktop", "e2e")) {
            return;
        }

        Path setupDir = projectSetupDir;
        if (!Files.exists(setupDir)) {
            setupDir = projectSetupDirFallback;
        }

        try (Stream<Path> files = Files.list(setupDir)) {
            files.forEach(file -> {
                if (Files.isDirectory(file)) {
                    try {
                        FileUtils.copyDirectory(file.toFile(), projectRoot.resolve(file.getFileName()).toFile(),
                                FileFilterUtils.trueFileFilter(), true, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new ArtivactException("Could not copy project directory!", e);
                    }
                } else {
                    copyProjectResource(file, projectRoot.resolve(file.getFileName()));
                }
            });
        } catch (IOException e) {
            throw new ArtivactException("Could not update project files!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void emptyDir(Path directory) {
        delete(directory);
        createDirIfRequired(directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("javasecurity:S2083") // Path is not entered by user!
    public void createDirIfRequired(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new ArtivactException("Could not create directory: " + directory, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("javasecurity:S2083") // Path is not entered by user!
    public void delete(Path path) {
        try {
            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    @Nonnull
                    public FileVisitResult visitFile(@NonNull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    @Nonnull
                    public FileVisitResult postVisitDirectory(@NonNull Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not delete path!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openDirInOs(Path directory) {
        var osString = System.getProperty("os.name");
        String[] cmdArray;
        if (osString.contains("Windows")) {
            cmdArray = new String[]{"cmd", "/c", "start", directory.toAbsolutePath().toString()};
        } else {
            cmdArray = new String[]{"xdg-open", directory.toAbsolutePath().toString()};
        }
        try {
            Runtime.getRuntime().exec(cmdArray);
        } catch (IOException e) {
            throw new ArtivactException("Could not open directory!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path source, Path target, CopyOption... copyOptions) {
        try {
            if (Files.exists(source) && Files.isDirectory(source)) {
                FileUtils.copyDirectory(source.toFile(), target.toFile());
            } else if (Files.exists(source)) {
                Files.copy(source, target, copyOptions);
            }
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_COPY_RESOURCE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Path source, Path target, CopyOption... copyOptions) {
        try {
            Files.move(source, target, copyOptions);
        } catch (IOException e) {
            throw new ArtivactException("Could not move resource!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("javasecurity:S2083") // Path is not entered by user!
    public void copy(InputStream source, Path target, CopyOption... copyOptions) {
        try {
            Files.copy(source, target, copyOptions);
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_COPY_RESOURCE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long copy(Path source, OutputStream target) {
        try {
            return Files.copy(source, target);
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_COPY_RESOURCE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getDirFromId(Path root, String id) {
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getSubdirFilePath(Path root, String id, String dirOrFilename) {
        if (dirOrFilename != null) {
            return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id).resolve(dirOrFilename);
        }
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubDir(String id, int index) {
        if (index == 0) {
            return id.substring(0, 3);
        } else if (index == 1) {
            return id.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Path> list(Path dir) {
        if (!Files.exists(dir)) {
            return List.of();
        }
        try {
            try (Stream<Path> files = Files.list(dir)) {
                return files.toList();
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not list files!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("java:S6204") // Result list needs to be mutable!
    @Override
    public List<String> listNamesWithoutScaledImages(Path path) {
        if (exists(path)) {
            return list(path).stream()
                    .filter(filePath -> !isDir(filePath))
                    .map(filePath -> filePath.getFileName().toString())
                    .filter(file -> {
                        for (ImageSize imageSize : ImageSize.values()) {
                            if (file.startsWith(imageSize.name())) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw new ArtivactException("Could not determine date of last modification!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDir(Path path) {
        if (path == null) {
            return false;
        }
        return Files.isDirectory(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Path file, byte[] target) {
        try {
            Files.write(file, target);
        } catch (IOException e) {
            throw new ArtivactException("Could not write file to array!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleImage(Path originalImage, Path targetImage, int targetWidth) {
        try {
            log.debug("Scaling image from {} to {}", originalImage, targetImage);
            BufferedImage bufferedImage = ImageIO.read(originalImage.toFile());
            String[] fileNameParts = originalImage.getFileName().toString().split("\\.");
            String fileEnding = fileNameParts[fileNameParts.length - 1];
            scaleImage(bufferedImage, targetImage, fileEnding, targetWidth);
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_SCALE_IMAGE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleImage(InputStream originalImage, Path targetImage, String fileEnding, int targetWidth) {
        try {
            BufferedImage bufferedImage = ImageIO.read(originalImage);
            scaleImage(bufferedImage, targetImage, fileEnding, targetWidth);
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_SCALE_IMAGE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size(Path path) {
        if (path == null || !Files.exists(path)) {
            return 0;
        }
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new ArtivactException("Could not read file size!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pack(Path source, Path target) {
        ZipUtil.pack(source.toFile(), target.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("javasecurity:S2083") // Path is not entered by user!
    public void unpack(Path source, Path target) {
        ZipUtil.unpack(source.toFile(), target.toFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(Path source) {
        try {
            return Files.readString(source);
        } catch (IOException e) {
            throw new ArtivactException("Could not read source!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream readStream(Path source) {
        try {
            return Files.newInputStream(source);
        } catch (IOException e) {
            throw new ArtivactException("Could not read source as stream!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readBytes(Path source) {
        try {
            return Files.readAllBytes(source);
        } catch (IOException e) {
            throw new ArtivactException("Could not read source into byte array!", e);
        }
    }

    /**
     * Returns the formatted asset's filename.
     *
     * @param assetNumber The asset number (e.g. 3).
     * @param extension   The asset's file extension (e.g. glb).
     * @return The formatted asset name (e.g. 003.glb).
     */
    @Override
    public String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

    /**
     * Returns the next available asset number.
     *
     * @param assetDir The directory containing assets.
     * @return The next available asset number.
     */
    @Override
    public int getNextAssetNumber(Path assetDir) {
        var highestNumber = 0;
        if (!Files.exists(assetDir)) {
            try {
                Files.createDirectories(assetDir);
            } catch (IOException e) {
                throw new ArtivactException("Could not create asset directory!", e);
            }
        }
        try (Stream<Path> stream = Files.list(assetDir)) {
            List<Path> assets = stream.toList();
            for (Path path : assets) {
                String existingAssetNumber = path.getFileName().toString().split("\\.")[0];
                if (!existingAssetNumber.matches("\\d*")) {
                    continue;
                }
                var number = Integer.parseInt(existingAssetNumber);
                if (number > 1000) {
                    log.warn("Invalid asset number in filename: {} / {}", existingAssetNumber, path.getFileName());
                    throw new ArtivactException("Error during asset number determination!");
                }
                if (number > highestNumber) {
                    highestNumber = number;
                }
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAndPruneEmptyParents(Path directory) {
        delete(directory);

        Path firstParent = directory.getParent();
        if (list(firstParent).isEmpty()) {
            delete(firstParent);
        }

        Path secondParent = firstParent.getParent();
        if (list(secondParent).isEmpty()) {
            delete(secondParent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveFile(Path projectRoot, String itemId, String filename, InputStream data, String subDir, String requiredFileExtension, boolean keepAssetNumber) {
        Path targetDir = getSubdirFilePath(projectRoot.resolve(DirectoryDefinitions.ITEMS_DIR), itemId, subDir);

        int assetNumber = getNextAssetNumber(targetDir);
        if (keepAssetNumber) {
            assetNumber = Integer.parseInt(getFilenameWithoutExtension(filename).orElseThrow());
        }
        String fileExtension = getExtension(filename).orElseThrow();

        if (StringUtils.hasText(requiredFileExtension) && !requiredFileExtension.equals(fileExtension)) {
            throw new ArtivactException("Unsupported file format. Files must be in '" + requiredFileExtension + "' format!");
        }

        String assetName = getAssetName(assetNumber, fileExtension);

        Path targetPath = targetDir.resolve(assetName);

        copy(data, targetPath, StandardCopyOption.REPLACE_EXISTING);

        return assetName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveFile(Path targetPath, byte[] content) {
        try {
            Files.write(targetPath, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new ArtivactException("Could not write file!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getFilenameWithoutExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(0, filename.indexOf(".")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystemResource loadImage(Path root, String id, String filename, ImageSize targetSize, String imagesSubdir) {
        Path originalImagePath = root
                .resolve(id.substring(0, 3))
                .resolve(id.substring(3, 6))
                .resolve(id)
                .resolve(imagesSubdir)
                .resolve(filename);

        Path scaledImagePath = root
                .resolve(id.substring(0, 3))
                .resolve(id.substring(3, 6))
                .resolve(id)
                .resolve(imagesSubdir)
                .resolve(targetSize.name() + "-" + filename);

        if (!ImageSize.ORIGINAL.equals(targetSize) && !exists(scaledImagePath)) {
            scaleImage(originalImagePath, scaledImagePath, targetSize.getWidth());
        }

        if (ImageSize.ORIGINAL.equals(targetSize)) {
            return new FileSystemResource(originalImagePath);
        } else {
            return new FileSystemResource(scaledImagePath);
        }
    }

    /**
     * Copies a classpath resource into the filesystem.
     *
     * @param resource  The resource to copy.
     * @param targetDir The target directory in the filesystem.
     */
    private void copyProjectResource(Path resource, Path targetDir) {
        createDirIfRequired(targetDir);
        try {
            Files.copy(resource, targetDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy project resource!", e);
        }
    }

    /**
     * Scales an image to the desired size and writes it as new image file to disk.
     *
     * @param bufferedImage The original image.
     * @param targetImage   Path to the scaled image to write.
     * @param fileEnding    The file ending of the original image.
     * @param targetWidth   The desired target width of the scaled image.
     */
    private void scaleImage(BufferedImage bufferedImage, Path targetImage, String fileEnding, int targetWidth) {
        bufferedImage = Scalr.resize(bufferedImage, targetWidth);
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, fileEnding, byteArrayOutputStream);
            write(targetImage, byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_SCALE_IMAGE, e);
        }
    }

}
