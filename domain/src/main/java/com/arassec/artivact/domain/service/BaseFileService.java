package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base for services that need file handling.
 */
public abstract class BaseFileService {

    /**
     * Returns the application's {@link FileRepository}.
     *
     * @return The file util.
     */
    protected abstract FileRepository getFileRepository();

    /**
     * Lists all files from the given path in the given subdirectory. Image files automatically created by Artivact for
     * different image sizes are filtered.
     *
     * @param path   The path to the files.
     * @param subDir The (optional) directory containing the files.
     * @return List of filenames.
     */
    @SuppressWarnings("java:S6204") // Result list needs to be mutable!
    public List<String> getFiles(Path path, String subDir) {
        Path targetPath = path;
        if (subDir != null) {
            targetPath = targetPath.resolve(subDir);
        }
        if (getFileRepository().exists(targetPath)) {
            return getFileRepository().list(targetPath).stream()
                    .filter(filePath -> !getFileRepository().isDir(filePath))
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
            getFileRepository().createDirIfRequired(filePath.getParent());
            getFileRepository().copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArtivactException("Could not save file!", e);
        }

        return filename;
    }

    /**
     * Saves the provided import file in the project dir.
     *
     * @param file The uploaded import file to save.
     * @return Path into the project's directory structure to the import file.
     */
    @SuppressWarnings("javasecurity:S2083") // Path is not entered by user!
    protected Path saveFile(Path root, MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("fallback.zip");
        Path importFileZip = root
                .resolve(ProjectDataProvider.TEMP_DIR)
                .resolve(originalFilename)
                .toAbsolutePath();

        try {
            file.transferTo(importFileZip);
        } catch (IOException e) {
            throw new ArtivactException("Could not save uploaded ZIP file!", e);
        }

        return importFileZip;
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
        Path originalImagePath = root.resolve(id.substring(0, 3)).resolve(id.substring(3, 6)).resolve(id).resolve(imagesSubdir).resolve(filename);

        Path scaledImagePath = root.resolve(id.substring(0, 3)).resolve(id.substring(3, 6)).resolve(id).resolve(imagesSubdir).resolve(targetSize.name() + "-" + filename);

        if (!ImageSize.ORIGINAL.equals(targetSize) && !getFileRepository().exists(scaledImagePath)) {
            getFileRepository().scaleImage(originalImagePath, scaledImagePath, targetSize.getWidth());
        }

        if (ImageSize.ORIGINAL.equals(targetSize)) {
            return new FileSystemResource(originalImagePath);
        } else {
            return new FileSystemResource(scaledImagePath);
        }
    }

    /**
     * Deletes the given directory and both direct parent directories, if they are empty after deleting the original
     * directory.
     *
     * @param directory Path to the directory to delete.
     */
    protected void deleteDirAndEmptyParents(Path directory) {
        getFileRepository().delete(directory);

        Path firstParent = directory.getParent();
        if (getFileRepository().list(firstParent).isEmpty()) {
            getFileRepository().delete(firstParent);
        }

        Path secondParent = firstParent.getParent();
        if (getFileRepository().list(secondParent).isEmpty()) {
            getFileRepository().delete(secondParent);
        }
    }

    /**
     * Returns the file extension of the given filename.
     *
     * @param filename The name of the file.
     * @return The file extension.
     */
    protected Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
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
        return root.resolve(getFileRepository().getSubDir(id, 0)).resolve(getFileRepository().getSubDir(id, 1)).resolve(id).resolve(filename);
    }


}
