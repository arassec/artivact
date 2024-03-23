package com.arassec.artivact.backend.service.util;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.misc.FileModification;
import com.arassec.artivact.backend.service.misc.ProjectDataProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility for file handling.
 */
@Component
@RequiredArgsConstructor
public class FileUtil {

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
                    Path copiedFile = projectRoot.resolve(file.getFileName());
                    deleteDir(copiedFile);
                    try {
                        FileUtils.copyDirectory(file.toFile(), projectRoot.resolve(file.getFileName()).toFile());
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
                String fileContent = Files.readString(file);
                fileContent = fileContent.replace(fileModification.placeholder(), fileModification.replacement());
                Files.writeString(file, fileContent, StandardOpenOption.WRITE);
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
        deleteDir(directory);
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
     * Deletes a directory recursively.
     *
     * @param directory The directory to delete.
     */
    public void deleteDir(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<>() {
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
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not delete directory!", e);
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

}
