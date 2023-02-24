package com.arassec.artivact.common.model;

import com.arassec.artivact.common.exception.ArtivactException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public abstract class Artivact {

    public static final String DATA_DIR = "Data";

    public static final String IMAGES_DIR = "images";

    public static final String MODELS_DIR = "models";

    public abstract Path getProjectRoot();

    public abstract String getId();

    public Path getMainDir(boolean includeProjectRoot) {
        var firstSubDir = getSubDir(0);
        var secondSubDir = getSubDir(1);
        Path resultPath = Path.of(DATA_DIR, firstSubDir, secondSubDir, getId());
        if (includeProjectRoot) {
            return getProjectRoot().resolve(resultPath);
        }
        return resultPath;
    }

    public Path getFirstSubDir(boolean includeProjectRoot) {
        var firstSubDir = getSubDir(0);
        Path resultPath = Path.of(DATA_DIR, firstSubDir);
        if (includeProjectRoot) {
            return getProjectRoot().resolve(resultPath);
        }
        return resultPath;
    }

    public Path getSecondSubDir(boolean includeProjectRoot) {
        return getFirstSubDir(includeProjectRoot).resolve(getSubDir(1));
    }

    public Path getImagesDir(boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(getProjectRoot(), IMAGES_DIR);
        }
        return getAssetDir(null, IMAGES_DIR);
    }

    public Path getModelsDir(boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(getProjectRoot(), MODELS_DIR);
        }
        return getAssetDir(null, MODELS_DIR);
    }

    public Path getModelDir(boolean includeProjectRoot, int assetNumber) {
        return getModelsDir(includeProjectRoot).resolve(getAssetName(assetNumber, null));
    }

    public Path getImagePath(boolean includeProjectRoot, int assetNumber, String extension) {
        var firstSubDir = getSubDir(0);
        var secondSubDir = getSubDir(1);
        var imageName = getAssetName(assetNumber, extension);
        Path resultPath = Path.of(DATA_DIR, firstSubDir, secondSubDir, getId(), IMAGES_DIR, imageName);
        if (includeProjectRoot) {
            return getProjectRoot().resolve(resultPath);
        }
        return resultPath;
    }

    public int getNextAssetNumber(Path assetDir, List<String> excludedDirs) {
        var highestNumber = 0;
        if (!Files.exists(assetDir)) {
            try {
                Files.createDirectories(assetDir);
            } catch (IOException e) {
                throw new ArtivactException("Could not create asset directory!", e);
            }
        }
        try (Stream<Path> stream = Files.list(assetDir)) {
            List<Path> assets = stream.toList();
            for (Path path : assets) {
                if (excludedDirs.contains(path.getFileName().toString())
                        || ".".equals(path.getFileName().toString())
                        || "..".equals(path.getFileName().toString())) {
                    continue;
                }
                var number = Integer.parseInt(path.getFileName().toString().split("\\.")[0]);
                if (number > highestNumber) {
                    highestNumber = number;
                }
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    public String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

    public String getSubDir(int index) {
        if (index == 0) {
            return getId().substring(0, 3);
        } else if (index == 1) {
            return getId().substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

    protected Path getAssetDir(Path projectRoot, String assetSubDir) {
        var firstSubDir = getSubDir(0);
        var secondSubDir = getSubDir(1);
        Path resultPath = Path.of(DATA_DIR, firstSubDir, secondSubDir, getId(), assetSubDir);
        if (projectRoot != null) {
            return projectRoot.resolve(resultPath);
        }
        return resultPath;
    }

}
