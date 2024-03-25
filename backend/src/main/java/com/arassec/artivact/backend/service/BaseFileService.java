package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import com.arassec.artivact.backend.service.util.FileUtil;
import org.imgscalr.Scalr;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base for services that need file handling.
 */
public abstract class BaseFileService extends BaseService {

    /**
     * Returns the application's {@link FileUtil}.
     *
     * @return The file util.
     */
    protected abstract FileUtil getFileUtil();

    /**
     * Lists all files from the given path in the given subdirectory.
     *
     * @param path   The path to the files.
     * @param subDir The directory containing the files.
     * @return List of filenames.
     */
    @SuppressWarnings("java:S6204") // Result list needs to be mutable!
    public List<String> getFiles(Path path, String subDir) {
        Path targetPath = path;
        if (subDir != null) {
            targetPath = targetPath.resolve(subDir);
        }
        if (Files.exists(targetPath)) {
            try (Stream<Path> files = Files.list(targetPath)) {
                return files.filter(filePath -> !Files.isDirectory(filePath)).map(filePath -> filePath.getFileName().toString()).filter(file -> {
                    for (ImageSize imageSize : ImageSize.values()) {
                        if (file.startsWith(imageSize.name())) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());

            } catch (IOException e) {
                throw new ArtivactException("Could not read files from path: " + path, e);
            }
        }
        return new LinkedList<>();
    }

    /**
     * Saves the given file under subdirectories of the root directory based on the given ID.
     * <p>
     * E.g. with root "/path/to/dir" and ID "123ABC" the resulting location of the file will be under
     * "/path/to/dir/123/ABC/123ABC".
     *
     * @param root The root path to store the file in.
     * @param id   An ID which is used to generate subdirectories to place the file in.
     * @param file The file to save.
     * @return The name of the saved file.
     */
    protected String saveFile(Path root, String id, MultipartFile file) {
        String filename = file.getOriginalFilename();

        Path filePath = getSimpleFilePath(root, id, filename);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save file!", e);
        }

        return filename;
    }

    /**
     * Loads a file from a subdirectory of the root path based on the given ID.
     *
     * @param root       The root path to get the file from.
     * @param id         The ID to use to determine subdirectories.
     * @param filename   The name of the file to load.
     * @param targetSize The desired target size of an image, if that's to be loaded.
     * @return A {@link FileSystemResource} to the file.
     */
    protected FileSystemResource loadFile(Path root, String id, String filename, ImageSize targetSize) {
        if (targetSize != null) {
            return loadImage(root, id, filename, targetSize, ".");
        }
        return new FileSystemResource(getSimpleFilePath(root, id, filename));
    }

    /**
     * Loads an image from a subdirectory of the root path based on the given ID.
     *
     * @param root         The root path to get the file from.
     * @param id           The ID to use to determine subdirectories.
     * @param filename     The filename of the image to load.
     * @param targetSize   The image's desired target size.
     * @param imagesSubdir The subdirectory containing the images.
     * @return A {@link FileSystemResource} to the image.
     */
    protected FileSystemResource loadImage(Path root, String id, String filename, ImageSize targetSize, String imagesSubdir) {
        try {
            Path originalImagePath = root.resolve(id.substring(0, 3)).resolve(id.substring(3, 6)).resolve(id).resolve(imagesSubdir).resolve(filename);

            Path scaledImagePath = root.resolve(id.substring(0, 3)).resolve(id.substring(3, 6)).resolve(id).resolve(imagesSubdir).resolve(targetSize.name() + "-" + filename);

            String[] fileNameParts = filename.split("\\.");
            String fileEnding = fileNameParts[fileNameParts.length - 1];

            BufferedImage bufferedImage;

            if (!ImageSize.ORIGINAL.equals(targetSize) && !Files.exists(scaledImagePath)) {
                bufferedImage = ImageIO.read(originalImagePath.toFile());
                bufferedImage = Scalr.resize(bufferedImage, targetSize.getWidth());

                try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
                    ImageIO.write(bufferedImage, fileEnding, byteArrayOutputStream);
                    Files.write(scaledImagePath, byteArrayOutputStream.toByteArray());
                }
            }

            if (ImageSize.ORIGINAL.equals(targetSize)) {
                return new FileSystemResource(originalImagePath);
            } else {
                return new FileSystemResource(scaledImagePath);
            }

        } catch (IOException e) {
            throw new ArtivactException("Could not read artivact image file!", e);
        }
    }

    /**
     * Returns the path to the subdir based on the root path and subdirectories based on the given ID.
     * <p>
     * E.g. a root of '/root/path' and an ID of 'ABC123' with subdir 'subdir' will lead to the path
     * '/root/path/ABC/123/ABC123/subdir'.
     *
     * @param root   The root path to start from.
     * @param id     The ID to use for subdirectory generation.
     * @param subdir The final subdir at the end of the path.
     * @return The path to the subdir.
     */
    protected Path getSubdirFilePath(Path root, String id, String subdir) {
        if (subdir != null) {
            return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id).resolve(subdir);
        }
        return  root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
    }

    /**
     * Returns a path to the subdirectory folder for the given ID.
     * <p>
     * E.g. a root of '/root/path' and an ID of 'ABC123' will lead to the path '/root/path/ABC/123/ABC123'.
     *
     * @param root The root path to start from.
     * @param id   The ID to use for subdirectory generation.
     * @return The path to the subdir.
     */
    protected Path getDirFromId(Path root, String id) {
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
    }

    /**
     * Deletes the given directory and both direct parent directories, if they are empty after deleting the original
     * directory.
     *
     * @param directory Path to the directory to delete.
     */
    protected void deleteDirAndEmptyParents(Path directory) {
        try {
            getFileUtil().deleteDir(directory);

            Path firstParent = directory.getParent();
            if (Files.exists(firstParent) && Objects.requireNonNull(firstParent.toFile().listFiles()).length == 0) {
                Files.delete(firstParent);
            }

            Path secondParent = firstParent.getParent();
            if (Files.exists(secondParent) && Objects.requireNonNull(secondParent.toFile().listFiles()).length == 0) {
                Files.delete(secondParent);
            }

        } catch (IOException e) {
            throw new ArtivactException("Could not delete directory!", e);
        }
    }

    /**
     * Returns the path to the desired file within an item's or widget's directory structure.
     * <p>
     * E.g. a root of '/root/path', an ID of 'ABC123' and a filename 'sample.txt' will lead to the path
     * '/root/path/ABC/123/ABC123/sample.txt'.
     *
     * @param root     The root path to start from.
     * @param id       The item's or widget's ID.
     * @param filename The file to get the path of.
     * @return The path to the file.
     */
    private Path getSimpleFilePath(Path root, String id, String filename) {
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id).resolve(filename);
    }

    /**
     * Returns a subdirectory from the given ID.
     * <p>
     * E.g. with an ID of 'ABC123' and an index of 1 will lead to the result: '123'.
     *
     * @param id    The ID to use for subdir determination.
     * @param index The index of the subdir to get.
     * @return The subdirectory as string.
     */
    private String getSubDir(String id, int index) {
        if (index == 0) {
            return id.substring(0, 3);
        } else if (index == 1) {
            return id.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }


}
