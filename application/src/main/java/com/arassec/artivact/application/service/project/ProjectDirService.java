package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * Service implementing the use case to retrieve the project's root directory.
 */
@Slf4j
@Service
public class ProjectDirService implements UseProjectDirsUseCase {

    /**
     * Path to the project's root directory.
     */
    @Getter
    private final Path projectRoot;

    /**
     * Creates a new instance.
     *
     * @param projectRoot The project's root directory as string.
     */
    public ProjectDirService(@Value("${artivact.project.root:avdata}") String projectRoot) {
        this.projectRoot = Path.of(projectRoot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getItemsDir() {
        return projectRoot.resolve(DirectoryDefinitions.ITEMS_DIR);
    }

    @Override
    public Path getWidgetsDir() {
        return projectRoot.resolve(DirectoryDefinitions.WIDGETS_DIR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getImagesDir(String itemId) {
        return getAssetDir(itemId, DirectoryDefinitions.IMAGES_DIR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getModelsDir(String itemId) {
        return getAssetDir(itemId, DirectoryDefinitions.MODELS_DIR);
    }

    /**
     * Returns the path to the asset subdirectory by appending item ID-based subdirectories to the project root
     * (if provided) and adding the supplied asset subdirectory.
     *
     * @param itemId      The item's (UU)ID.
     * @param assetSubDir The asset subdirectory (e.g. 'images') which will be appended to the path.
     * @return The Path to the asset subdirectory.
     */
    private Path getAssetDir(String itemId,String assetSubDir) {
        var firstSubDir = getSubDir(itemId, 0);
        var secondSubDir = getSubDir(itemId, 1);
        return getItemsDir()
                .resolve(firstSubDir)
                .resolve(secondSubDir)
                .resolve(itemId)
                .resolve(assetSubDir);
    }

    /**
     * Returns the subdirectory according to the Artivact-Creator's directory structure based on the item ID and an index.
     *
     * @param itemId The item's (UU)ID.
     * @param index  The index of the subdirectory.
     * @return The subdirectory with the given index based on the item's ID.
     */
    private String getSubDir(String itemId, int index) {
        if (index == 0) {
            return itemId.substring(0, 3);
        } else if (index == 1) {
            return itemId.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

}
