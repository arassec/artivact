package com.arassec.artivact.backend.service.util;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.misc.FileModification;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility for file handling.
 */
@Component
@RequiredArgsConstructor
public class FileUtil {

    /**
     * Suffix for ZIP files.
     */
    public static final String ZIP_FILE_SUFFIX = ".zip";

    /**
     * Suffix for JSON files.
     */
    public static final String JSON_FILE_SUFFIX = ".json";

    /**
     * Spring's {@link Environment}.
     */
    private final Environment environment;

    /**
     * Initializes or updates the project directory by copying project files from the classpath and updating necessary
     * file contents.
     *
     * @param projectRoot       The project's root directory.
     * @param fileModifications List of file modifications to perform during project setup.
     */
    public void updateProjectDirectory(Path projectRoot, List<FileModification> fileModifications) {

        if (!environment.matchesProfiles("desktop")) {
            return;
        }

        Path projectSetupDir = Path.of("resources/project-setup");
        if (!Files.exists(projectSetupDir)) {
            projectSetupDir = Path.of("backend/src/main/resources/project-setup");
        }
        createDirIfRequired(projectRoot.resolve(ProjectDataProvider.TEMP_DIR));

        try (Stream<Path> files = Files.list(projectSetupDir)) {
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
     * Empties the given directory.
     *
     * @param directory The directory to empty.
     */
    public void emptyDir(Path directory) {
        delete(directory);
        createDirIfRequired(directory);
    }

    /**
     * Creates a directory (and required parent directories) if it doesn't exist.
     *
     * @param directory The director to create if required.
     */
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
     * Deletes a file or a complete path recursively.
     *
     * @param path The path to delete.
     */
    public void delete(Path path) {
        try {
            if (Files.exists(path) && Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
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
     * Opens a directory with the operating systems default file manager.
     *
     * @param directory The directory to open.
     */
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
     * Checks if a path exists or not.
     *
     * @param path The path to check.
     * @return {@code true}, if the path exists, {@code false} otherwise.
     */
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Copies a file from source to target.
     *
     * @param source      The source path.
     * @param target      The target path.
     * @param copyOptions The options to use for copying.
     * @return The path to the target file.
     */
    public Path copy(Path source, Path target, CopyOption... copyOptions) {
        try {
            return Files.copy(source, target, copyOptions);
        } catch (IOException e) {
            throw new ArtivactException("Could not copy resource!", e);
        }
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
    public Path getDirFromId(Path root, String id) {
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
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
    public Path getSubdirFilePath(Path root, String id, String subdir) {
        if (subdir != null) {
            return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id).resolve(subdir);
        }
        return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id);
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
    public String getSubDir(String id, int index) {
        if (index == 0) {
            return id.substring(0, 3);
        } else if (index == 1) {
            return id.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

    /**
     * Lists the files in the given directory.
     *
     * @param dir The directory to list files of.
     * @return Stream of paths found in the given directory.
     */
    public Stream<Path> list(Path dir) {
        try {
            return Files.list(dir);
        } catch (IOException e) {
            throw new ArtivactException("Could not list files!", e);
        }
    }

    /**
     * Returns the last modification time of a file or directory.
     *
     * @param path The path to get the time from.
     * @return The last modification time within the system's timezone.
     */
    public ZonedDateTime lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
