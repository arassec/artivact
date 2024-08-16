package com.arassec.artivact.core.repository;

import com.arassec.artivact.core.misc.FileModification;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Repository for files and directories.
 */
public interface FileRepository {

    /**
     * Suffix for ZIP files.
     */
    String ZIP_FILE_SUFFIX = ".zip";

    /**
     * Suffix for JSON files.
     */
    String JSON_FILE_SUFFIX = ".json";

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
     * Deletes a file or a complete directory recursively.
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
     */
    void copy(Path source, OutputStream target);

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
     * @param root   The root path to start from.
     * @param id     The ID to use for subdirectory generation.
     * @param subdir The final subdir at the end of the path.
     * @return The path to the subdir.
     */
    Path getSubdirFilePath(Path root, String id, String subdir);

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
    Stream<Path> list(Path dir);

    /**
     * Returns the last modification time of a file or directory.
     *
     * @param path The path to get the time from.
     * @return The last modification time.
     */
    Instant lastModified(Path path);

}
