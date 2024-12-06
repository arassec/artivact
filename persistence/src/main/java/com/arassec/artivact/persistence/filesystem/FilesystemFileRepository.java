package com.arassec.artivact.persistence.filesystem;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.FileModification;
import com.arassec.artivact.core.repository.FileRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.imgscalr.Scalr;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
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
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link FileRepository} implementation that uses the operating system's filesystem.
 */
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
    public void updateProjectDirectory(Path projectRoot, Path projectSetupDir, Path projectSetupDirFallback, List<FileModification> fileModifications) {

        if (!environment.matchesProfiles("desktop")) {
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

        fileModifications.forEach(fileModification -> {
            try {
                Path file = projectRoot.resolve(fileModification.file());
                if (!Files.exists(file)) {
                    Path templateFile = projectRoot.resolve(fileModification.file() + ".tpl");
                    String fileContent = Files.readString(templateFile);
                    fileContent = fileContent.replace(fileModification.placeholder(), fileModification.replacement());
                    Files.writeString(file, fileContent, StandardOpenOption.CREATE_NEW);
                }
            } catch (IOException e) {
                throw new ArtivactException("Could not update project file!", e);
            }
        });
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
    public void delete(Path path) {
        try {
            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    @Nonnull
                    public FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    @Nonnull
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
            Files.copy(source, target, copyOptions);
        } catch (IOException e) {
            throw new ArtivactException(COULD_NOT_COPY_RESOURCE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public void copyDir(Path source, Path target) {
        try {
            if (Files.exists(source) && Files.isDirectory(source)) {
                FileUtils.copyDirectory(source.toFile(), target.toFile());
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not copy directory!", e);
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
