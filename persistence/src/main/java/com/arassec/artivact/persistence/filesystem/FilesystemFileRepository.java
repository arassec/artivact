package com.arassec.artivact.persistence.filesystem;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.FileModification;
import com.arassec.artivact.core.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@link FileRepository} implementation that uses the operating system's filesystem.
 */
@Component
@RequiredArgsConstructor
public class FilesystemFileRepository implements FileRepository {

    /**
     * Spring's {@link Environment}.
     */
    private final Environment environment;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProjectDirectory(Path projectRoot, List<FileModification> fileModifications, String tempDir) {

        if (!environment.matchesProfiles("desktop")) {
            return;
        }

        Path projectSetupDir = Path.of("resources/project-setup");
        if (!Files.exists(projectSetupDir)) {
            projectSetupDir = Path.of("domain/src/main/resources/project-setup");
        }
        createDirIfRequired(projectRoot.resolve(tempDir));

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
            throw new ArtivactException("Could not copy resource!", e);
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
            throw new ArtivactException("Could not copy resource!", e);
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
    public Path getSubdirFilePath(Path root, String id, String subdir) {
        if (subdir != null) {
            return root.resolve(getSubDir(id, 0)).resolve(getSubDir(id, 1)).resolve(id).resolve(subdir);
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
    public Stream<Path> list(Path dir) {
        try {
            return Files.list(dir);
        } catch (IOException e) {
            throw new ArtivactException("Could not list files!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault());
        } catch (IOException e) {
            throw new ArtivactException("Could not determine date of last modification!", e);
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

}
