package com.arassec.artivact.backend.service.util;

import com.arassec.artivact.backend.service.exception.ArtivactException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Utility for file handling.
 */
@Component
public class FileUtil {

    /**
     * Initializes or updates the project directory by copying project files from the classpath and updating necessary
     * file contents.
     *
     * @param projectRoot       The project's root directory.
     * @param projectSetupDir   The classpath directory containing the setup files for projects.
     * @param fileModifications List of file modifications to perform during project setup.
     */
    public void updateProjectDirectory(Path projectRoot, Path projectSetupDir, List<FileModification> fileModifications) {
        createDirIfRequired(projectRoot.resolve("temp"));
        copyClasspathResource(projectSetupDir, projectRoot);
        fileModifications.forEach(fileModification -> {
            try {
                Path metashapeWorkflowFile = projectRoot.resolve(fileModification.file());
                String metashapeWorkflow = Files.readString(metashapeWorkflowFile);
                metashapeWorkflow = metashapeWorkflow.replace(fileModification.placeholder(), fileModification.replacement());
                Files.writeString(metashapeWorkflowFile, metashapeWorkflow, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new ArtivactException("Could not update project file!", e);
            }
        });
    }

    /**
     * Saves the provided String in the given file.
     *
     * @param file     Path to the file to save.
     * @param contents The file contents.
     */
    public void saveFile(Path file, String contents) {
        try {
            Files.writeString(file, contents);
        } catch (IOException e) {
            throw new ArtivactException("Could not save file!", e);
        }
    }

    /**
     * Reads the given file and returns the content as String.
     *
     * @param file The file to read.
     * @return The file's contents.
     */
    public String readFile(Path file) {
        if (!Files.exists(file)) {
            throw new ArtivactException("File to read does not exist: " + file);
        }

        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new ArtivactException("Could not read file!", e);
        }
    }

    /**
     * Checks if a directory is empty or not.
     *
     * @param directory The directory to check.
     * @return {@code true} if the directory isempty, {@code false} otherwise.
     */
    public boolean isDirEmpty(final Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new ArtivactException("Could not check directory!", e);
        }
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
     * @param classpathResource The resource to copy.
     * @param targetDir         The target directory in the filesystem.
     */
    public void copyClasspathResource(Path classpathResource, Path targetDir) {
        createDirIfRequired(targetDir);

        String sourceDir = classpathResource.toString().replace('\\', '/');

        var classLoader = getClass().getClassLoader();
        try {
            var resourceUrl = classLoader.getResource(sourceDir);
            if (resourceUrl == null) {
                throw new ArtivactException("Could not open resource: " + sourceDir);
            }
            var resourceDir = resourceUrl.toURI();

            if (resourceDir.toString().contains("!")) {
                copyClasspathResourcesFromJar(resourceDir, targetDir);
            } else {
                copyClasspathResourceFromFilesystem(Path.of(Objects.requireNonNull(resourceDir)), targetDir);
            }

        } catch (URISyntaxException | IOException e) {
            throw new ArtivactException("Could not create initial project layout!", e);
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
     * @return The process created by opening the directories.
     */
    public Process openDirInOs(Path directory) {
        var osString = System.getProperty("os.name");
        String[] cmdArray;
        if (osString.contains("Windows")) {
            cmdArray = new String[]{"cmd", "/c", "start", directory.toAbsolutePath().toString()};
        } else {
            cmdArray = new String[]{"xdg-open", directory.toAbsolutePath().toString()};
        }
        try {
            return Runtime.getRuntime().exec(cmdArray);
        } catch (IOException e) {
            throw new ArtivactException("Could not open directory!", e);
        }
    }

    /**
     * Checks whether a file is executable or not.
     *
     * @param filePath Path to the file to check.
     * @return {@code true} if the file is executable, {@code false} otherwise.
     */
    public boolean isFileExecutable(String filePath) {
        Path path = Path.of(filePath);
        return Files.exists(path) && Files.isExecutable(path);
    }

    /**
     * Copies a classpath resource from the JAR if the application is running regularly.
     *
     * @param resourceUri The resource URI to copy.
     * @param targetDir   The target directory.
     * @throws IOException In case of errors during filesystem copying.
     */
    private void copyClasspathResourcesFromJar(URI resourceUri, Path targetDir) throws IOException {
        String[] parts = resourceUri.toString().split("!");
        try (var fs = FileSystems.newFileSystem(URI.create(parts[0]), new HashMap<>())) {
            var resourcesPath = fs.getPath(parts[1], parts[2]);
            copyClasspathResourceFromFilesystem(resourcesPath, targetDir);
        }
    }

    /**
     * Copies a classpath resource from the filesystem in case the application is run from within an IDE.
     *
     * @param resourcesPath The resource directory to copy.
     * @param targetDir     The target directory.
     * @throws IOException In case of errors during filesystem copying.
     */
    private void copyClasspathResourceFromFilesystem(Path resourcesPath, Path targetDir) throws IOException {
        Files.walkFileTree(resourcesPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                var subDir = resourcesPath.relativize(dir);
                if (StringUtils.hasText(subDir.toString())) {
                    var tarDir = targetDir.resolve(subDir.toString());
                    Files.createDirectories(tarDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var subFile = resourcesPath.relativize(file);
                if (StringUtils.hasText(subFile.toString())) {
                    var tarFile = targetDir.resolve(subFile.toString());
                    Files.copy(file, tarFile, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(file, targetDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }

        });
    }

}
