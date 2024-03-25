package com.arassec.artivact.backend.service.misc;

import com.arassec.artivact.backend.service.util.FileUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Provider for the project's root directory and other project related directories.
 */
@Component
@Getter
public class ProjectDataProvider {

    /**
     * The directory containing the project's items.
     */
    public static final String ITEMS_DIR = "items";

    /**
     * The directory containing the project's exhibitions.
     */
    public static final String EXHIBITIONS_DIR = "exhibitions";

    /**
     * The directory containing an item's images.
     */
    public static final String IMAGES_DIR = "images";

    /**
     * The directory containing an item's models.
     */
    public static final String MODELS_DIR = "models";

    /**
     * The directory containing temporary files during e.g. model creation.
     */
    public static final String TEMP_DIR = "temp";

    /**
     * Directory containing additional data (like files) from widgets.
     */
    public static final String WIDGETS_FILE_DIR = "widgets";

    /**
     * Directory containing search-engine data.
     */
    public static final String SEARCH_INDEX_DIR = "sedata";

    /**
     * Path to the project's root directory.
     */
    private final Path projectRoot;

    /**
     * The application's {@link FileUtil}.
     */
    private final FileUtil fileUtil;

    /**
     * Creates a new instance.
     *
     * @param projectRootString The path to the project root, injected by Spring.
     */
    public ProjectDataProvider(@Value("${artivact.project.root:avdata}") String projectRootString,
                               FileUtil fileUtil) {
        this.projectRoot = Path.of(projectRootString);
        this.fileUtil = fileUtil;
    }

    /**
     * Updates the project's filesystem structure.
     */
    @PostConstruct
    public void initializeProjectDir() {
        fileUtil.updateProjectDirectory(projectRoot, List.of(
                new FileModification("utils/Metashape/artivact-metashape-workflow.xml", "##EXPORT_PATH##",
                        projectRoot.resolve("temp/export/metashape-export.obj").toAbsolutePath().toString()),
                new FileModification("utils/Meshroom/artivact-meshroom-workflow.mg", "##EXPORT_PATH##",
                        projectRoot.resolve("temp/export/").toAbsolutePath().toString().replace("\\", "/"))
        ));
    }

}
