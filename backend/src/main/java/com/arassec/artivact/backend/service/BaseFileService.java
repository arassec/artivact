package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.service.exception.VaultException;
import com.arassec.artivact.backend.service.model.item.ImageSize;
import org.imgscalr.Scalr;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseFileService extends BaseService {

    protected static final String ITEMS_FILE_DIR = "items";

    public static final String IMAGES_DIR = "images";

    public static final String MODELS_DIR = "models";

    @SuppressWarnings("java:S6204") // Result list needs to be mutable!
    public List<String> getFiles(Path path, String subDir) {
        if (Files.exists(path.resolve(subDir))) {
            try (Stream<Path> files = Files.list(path.resolve(subDir))) {
                return files.map(filePath -> filePath.getFileName().toString())
                        .filter(file -> {
                            for (ImageSize imageSize : ImageSize.values()) {
                                if (file.startsWith(imageSize.name())) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .collect(Collectors.toList());

            } catch (IOException e) {
                throw new VaultException("Could not read files from path: " + path, e);
            }
        }
        return new LinkedList<>();
    }

    protected String saveFile(Path root, String id, MultipartFile file) {
        String fileName = file.getOriginalFilename();

        Path filePath = getSimpleFilePath(root, id, fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new VaultException("Could not save file!", e);
        }

        return fileName;
    }

    protected FileSystemResource loadFile(Path root, String id, String fileName, ImageSize targetSize) {
        if (targetSize != null) {
            return loadImage(root, id, fileName, targetSize, ".");
        }
        return new FileSystemResource(getSimpleFilePath(root, id, fileName));
    }

    protected FileSystemResource loadImage(Path root, String id, String fileName, ImageSize targetSize, String imagesSubdir) {
        try {
            Path originalImagePath = root
                    .resolve(id.substring(0, 3))
                    .resolve(id.substring(3, 6))
                    .resolve(id)
                    .resolve(imagesSubdir)
                    .resolve(fileName);

            Path scaledImagePath = root
                    .resolve(id.substring(0, 3))
                    .resolve(id.substring(3, 6))
                    .resolve(id)
                    .resolve(imagesSubdir)
                    .resolve(targetSize.name() + "-" + fileName);

            String[] fileNameParts = fileName.split("\\.");
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
            throw new VaultException("Could not read artivact image file!", e);
        }
    }

    protected Path getSubdirFilePath(Path root, String id, String subdir) {
        return root.resolve(getSubDir(id, 0))
                .resolve(getSubDir(id, 1))
                .resolve(id)
                .resolve(subdir);
    }

    protected Path getDirFromId(Path root, String id) {
        return root.resolve(getSubDir(id, 0))
                .resolve(getSubDir(id, 1))
                .resolve(id);
    }

    protected void deleteDirAndEmptyParents(Path directory) {
        try {
           deleteDirRecursively(directory);

            Path firstParent = directory.getParent();
            if (Files.exists(firstParent) && Objects.requireNonNull(firstParent.toFile().listFiles()).length == 0) {
                Files.delete(firstParent);
            }

            Path secondParent = firstParent.getParent();
            if (Files.exists(secondParent) && Objects.requireNonNull(secondParent.toFile().listFiles()).length == 0) {
                Files.delete(secondParent);
            }

        } catch (IOException e) {
            throw new VaultException("Could not delete directory!", e);
        }
    }

    protected void deleteDirRecursively(Path directory) {
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
            throw new VaultException("Could not delete directory!", e);
        }
    }

    protected void createDirIfRequired(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new VaultException("Could not create directory: " + directory, e);
            }
        }
    }

    private Path getSimpleFilePath(Path root, String id, String fileName) {
        return root.resolve(getSubDir(id, 0))
                .resolve(getSubDir(id, 1))
                .resolve(id)
                .resolve(fileName);
    }

    private String getSubDir(String id, int index) {
        if (index == 0) {
            return id.substring(0, 3);
        } else if (index == 1) {
            return id.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }


}
