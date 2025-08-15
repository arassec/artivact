package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.misc.FileModification;
import org.springframework.core.io.FileSystemResource;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for files and directories.
 */
public interface FileRepository {

    /**
     * Initializes or updates the project directory by copying project files from the classpath and updating necessary
     * file contents.
     *
     * @param projectRoot             The project's root directory.
     * @param projectSetupDir         The project-setup directory containing files to copy.
     * @param projectSetupDirFallback Fallback project-setup directory to use if projectSetupDir does not exist.
     * @param fileModifications       List of file modifications to perform during project setup.
     */
    void updateProjectDirectory(Path projectRoot, Path projectSetupDir, Path projectSetupDirFallback, List<FileModification> fileModifications);

    /**
     * Empties the given directory.
     *
     * @param directory The directory to empty.
     */
    void emptyDir(Path directory);

    /**
     * Creates a directory (and required parent directories) if it doesn't exist.
     *
     * @param directory The director to create if required.
     */
    void createDirIfRequired(Path directory);

    /**
     * Deletes a file or a complete directory recursively if it exists.
     *
     * @param path The path to delete.
     */
    void delete(Path path);

    /**
     * Opens a directory with the operating systems default file manager.
     *
     * @param directory The directory to open.
     */
    void openDirInOs(Path directory);

    /**
     * Checks if a path exists or not.
     *
     * @param path The path to check.
     * @return {@code true}, if the path exists, {@code false} otherwise.
     */
    boolean exists(Path path);

    /**
     * Copies a file from source to target.
     *
     * @param source      The source path.
     * @param target      The target path.
     * @param copyOptions The options to use for copying.
     */
    void copy(Path source, Path target, CopyOption... copyOptions);

    /**
     * Copies a source InputStream to a target path.
     *
     * @param source      The source {@link InputStream}.
     * @param target      The target path.
     * @param copyOptions The options to use for copying.
     */
    void copy(InputStream source, Path target, CopyOption... copyOptions);

    /**
     * Copies a source file into a target output stream.
     *
     * @param source The source {@link Path}.
     * @param target The target streams.
     * @return The number of bytes written to the output stream.
     */
    long copy(Path source, OutputStream target);

    /**
     * Returns a path to the subdirectory folder for the given ID.
     * <p>
     * E.g. a root of '/root/path' and an ID of 'ABC123' will lead to the path '/root/path/ABC/123/ABC123'.
     *
     * @param root The root path to start from.
     * @param id   The ID to use for subdirectory generation.
     * @return The path to the subdir.
     */
    Path getDirFromId(Path root, String id);

    /**
     * Returns the path to the subdir based on the root path and subdirectories based on the given ID.
     * <p>
     * E.g. a root of '/root/path' and an ID of 'ABC123' with subdir 'subdir' will lead to the path
     * '/root/path/ABC/123/ABC123/subdir'.
     *
     * @param root          The root path to start from.
     * @param id            The ID to use for subdirectory generation.
     * @param dirOrFilename The final directory of the path or a filename at the end of the path.
     * @return The path to the directory or file.
     */
    Path getSubdirFilePath(Path root, String id, String dirOrFilename);

    /**
     * Returns a subdirectory from the given ID.
     * <p>
     * E.g. with an ID of 'ABC123' and an index of 1 will lead to the result: '123'.
     *
     * @param id    The ID to use for subdir determination.
     * @param index The index of the subdir to get.
     * @return The subdirectory as string.
     */
    String getSubDir(String id, int index);

    /**
     * Lists the files in the given directory.
     *
     * @param dir The directory to list files of.
     * @return Stream of paths found in the given directory.
     */
    List<Path> list(Path dir);

    /**
     * Lists all file names from the given path in the given subdirectory. Image files automatically created by Artivact
     * for different image sizes are filtered.
     *
     * @param dir The directory to list files of.
     * @return List of filenames.
     */
    List<String> listNamesWithoutScaledImages(Path dir);

    /**
     * Returns the last modification time of a file or directory.
     *
     * @param path The path to get the time from.
     * @return The last modification time.
     */
    Instant lastModified(Path path);

    /**
     * Returns whether the provided path is a directory or not.
     *
     * @param path Path to the directory.
     * @return {@code true} if the provided path points to a directory, {@code false} otherwise.
     */
    boolean isDir(Path path);

    /**
     * Writes bytes to a file.
     *
     * @param file   Path to the file to write.
     * @param target The array to write to the file.
     */
    void write(Path file, byte[] target);

    /**
     * Scales the original image to the desired width and stores it in a new image file.
     *
     * @param originalImage The original image to scale.
     * @param targetImage   The target image to write.
     * @param targetWidth   The desired width of the target image.
     */
    void scaleImage(Path originalImage, Path targetImage, int targetWidth);

    /**
     * Scales the image provided by the input stream to the desired width and writes it as a new image file.
     *
     * @param originalImage The original image to scale.
     * @param targetImage   The target image to write.
     * @param fileEnding    The file ending of the original file, e.g. "jpeg" or "png".
     * @param targetWidth   The desired width of the target image.
     */
    void scaleImage(InputStream originalImage, Path targetImage, String fileEnding, int targetWidth);

    /**
     * Returns the size of the given file.
     *
     * @param path Path to the file to get the size of.
     * @return The size in bytes.
     */
    long size(Path path);

    /**
     * Packs the source using ZIP.
     *
     * @param source Source to pack.
     * @param target Target to pack the source into.
     */
    void pack(Path source, Path target);

    /**
     * Unpacks the source using ZIP.
     *
     * @param source The source to unpack.
     * @param target Target to unpack the source to.
     */
    void unpack(Path source, Path target);

    /**
     * Reads a source into a String.
     *
     * @param source Path to the source to read.
     * @return The source's content.
     */
    String read(Path source);

    /**
     * Reads a source into an {@link InputStream}.
     *
     * @param source The source to read.
     * @return A newly created input stream.
     */
    InputStream readStream(Path source);

    /**
     * Reads the provided source into a byte array.
     *
     * @param source The source to read.
     * @return A byte array containing the source's data.
     */
    byte[] readBytes(Path source);

    /**
     * Returns the formatted asset's filename.
     *
     * @param assetNumber The asset number e.g., 3.
     * @param extension   The asset's file extension e.g., "glb".
     * @return The formatted asset name e.g., "003.glb".
     */
    String getAssetName(int assetNumber, String extension);

    /**
     * Returns the next available asset number.
     *
     * @param assetDir The directory containing assets.
     * @return The next available asset number.
     */
    int getNextAssetNumber(Path assetDir);

    /**
     * Deletes the given directory and both direct parent directories if they are empty after deleting the original
     * directory.
     *
     * @param directory Path to the directory to delete.
     */
    void deleteDirAndEmptyParents(Path directory);

    /**
     * Saves a file to the item and provides it with a new asset number (if required).
     *
     * @param projectRoot           The project's root directory.
     * @param itemId                The item's ID.
     * @param filename              The file to save.
     * @param data                  The file's data as {@link InputStream}.
     * @param subDir                The subdirectory inside the item's directory to save the file to.
     * @param requiredFileExtension Provide to restrict the allowed file's extension.
     * @param keepAssetNumber       Set to {@code true}, if the asset number from the model's filename should be used.
     * @return The new file name.
     */
    String saveFile(Path projectRoot, String itemId, String filename, InputStream data, String subDir, String requiredFileExtension, boolean keepAssetNumber);

    /**
     * Saves a new file.
     *
     * @param targetPath The target path for the new file.
     * @param content    The file's content.
     */
    void saveFile(Path targetPath, byte[] content);

    /**
     * Returns the raw filename without file extension.
     *
     * @param filename The complete name of the file.
     * @return The name without extension.
     */
    Optional<String> getFilenameWithoutExtension(String filename);

    /**
     * Returns the file extension of the given filename.
     *
     * @param filename The name of the file.
     * @return The file extension.
     */
    Optional<String> getExtension(String filename);

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
    FileSystemResource loadImage(Path root, String id, String filename, ImageSize targetSize, String imagesSubdir);

}
