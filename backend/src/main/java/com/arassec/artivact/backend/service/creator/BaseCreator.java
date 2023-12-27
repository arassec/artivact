package com.arassec.artivact.backend.service.creator;

import com.arassec.artivact.backend.service.creator.adapter.Adapter;
import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.creator.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.backend.service.creator.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.backend.service.creator.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.ProjectDir;
import com.arassec.artivact.backend.service.model.configuration.AdapterConfiguration;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public abstract class BaseCreator {

    /**
     * Returns all available adapters.
     *
     * @return List of available adapters;
     */
    protected abstract List<Adapter<?, ?>> getAdapters();

    protected abstract ProjectRootProvider getProjectRootProvider();

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link TurntableAdapter}.
     */
    protected TurntableAdapter getTurntableAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getTurntableAdapterImplementation();
        return getAdapters().stream()
                .filter(TurntableAdapter.class::isInstance)
                .map(TurntableAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link CameraAdapter}.
     */
    protected CameraAdapter getCameraAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getCameraAdapterImplementation();
        return getAdapters().stream()
                .filter(CameraAdapter.class::isInstance)
                .map(CameraAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link BackgroundRemovalAdapter}.
     */
    protected BackgroundRemovalAdapter getBackgroundRemovalAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getBackgroundRemovalAdapterImplementation();
        return getAdapters().stream()
                .filter(BackgroundRemovalAdapter.class::isInstance)
                .map(BackgroundRemovalAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link ModelCreatorAdapter}.
     */
    protected ModelCreatorAdapter getModelCreatorAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getModelCreatorImplementation();
        return getAdapters().stream()
                .filter(ModelCreatorAdapter.class::isInstance)
                .map(ModelCreatorAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Returns the desired adapter.
     *
     * @param adapterConfiguration The current adapter configuration.
     * @return The configured {@link ModelEditorAdapter}.
     */
    protected ModelEditorAdapter getModelEditorAdapter(AdapterConfiguration adapterConfiguration) {
        AdapterImplementation adapterImplementation = adapterConfiguration.getModelEditorImplementation();
        return getAdapters().stream()
                .filter(ModelEditorAdapter.class::isInstance)
                .map(ModelEditorAdapter.class::cast)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

    /**
     * Returns the images directory of the currently active item.
     *
     * @param itemId             The ID of the item to get the images directory for.
     * @param includeProjectRoot Set to {@code true} to append the images directory path to the project's root directory.
     * @return The path to the currently active item's images directory.
     */
    public Path getImagesDir(String itemId, boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(itemId, getProjectRootProvider().getProjectRoot(), ProjectDir.IMAGES_DIR);
        }
        return getAssetDir(itemId, null, ProjectDir.IMAGES_DIR);
    }

    /**
     * Returns the subdirectory according to the Artivact-Creator's directory structure based on the item ID and an index.
     *
     * @param itemId The item's (UU)ID.
     * @param index  The index of the subdirectory.
     * @return The subdirectory with the given index based on the item's ID.
     */
    protected String getSubDir(String itemId, int index) {
        if (index == 0) {
            return itemId.substring(0, 3);
        } else if (index == 1) {
            return itemId.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

    /**
     * Returns the path to the asset subdirectory by appending item ID-based subdirectories to the project root
     * (if provided) and adding the supplied asset subdirectory.
     *
     * @param itemId      The item's (UU)ID.
     * @param projectRoot Path to the project's root directory. If {@code null}, the resulting path will be relative.
     * @param assetSubDir The asset subdirectory (e.g. 'images') which will be appended to the path.
     * @return The Path to the asset subdirectory.
     */
    protected Path getAssetDir(String itemId, Path projectRoot, String assetSubDir) {
        var firstSubDir = getSubDir(itemId, 0);
        var secondSubDir = getSubDir(itemId, 1);
        Path resultPath = Path.of(ProjectDir.ITEMS_DIR, firstSubDir, secondSubDir, itemId, assetSubDir);
        if (projectRoot != null) {
            return projectRoot.resolve(resultPath);
        }
        return resultPath;
    }

    /**
     * Returns the formatted asset's filename.
     *
     * @param assetNumber The asset number (e.g. 3).
     * @param extension   The asset's file extension (e.g. glb).
     * @return The formatted asset name (e.g. 003.glb).
     */
    protected String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

    /**
     * Returns the next available asset number.
     *
     * @param assetDir The directory containing assets.
     * @return The next available asset number.
     */
    protected int getNextAssetNumber(Path assetDir) {
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
                String existingAssetNumber = path.getFileName().toString().split("\\.")[0];
                if (!existingAssetNumber.matches("[0-9]*")) {
                    continue;
                }
                var number = Integer.parseInt(existingAssetNumber);
                if (number > highestNumber) {
                    highestNumber = number;
                }
            }
        } catch (IOException e) {
            throw new ArtivactException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    /**
     * Formats the path according to UNIX conventions (Windows "\\" is replaced with "/") in order to avoid path
     * formatting errors e.g. in JSON files.
     *
     * @param path The path to format.
     * @return The formatted path.
     */
    protected String formatPath(Path path) {
        return path.toString().replace("\\", "/");
    }

}
