package com.arassec.artivact.domain.misc;

import com.arassec.artivact.core.misc.FileModification;
import com.arassec.artivact.core.repository.FileRepository;
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
     * The directory containing the project's exported content.
     */
    public static final String EXPORT_DIR = "exports";

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
     * The directory containing an item's images.
     */
    public static final String IMAGES_DIR = "images";

    /**
     * The directory containing an item's models.
     */
    public static final String MODELS_DIR = "models";

    /**
     * Path to the project's root directory.
     */
    private final Path projectRoot;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * Creates a new instance.
     *
     * @param projectRootString The path to the project root, injected by Spring.
     */
    public ProjectDataProvider(@Value("${artivact.project.root:avdata}") String projectRootString,
                               FileRepository fileRepository) {
        this.projectRoot = Path.of(projectRootString);
        this.fileRepository = fileRepository;
    }

    /**
     * Updates the project's filesystem structure.
     */
    @PostConstruct
    public void initializeProjectDir() {
        fileRepository.createDirIfRequired(projectRoot.resolve(ITEMS_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(EXPORT_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(TEMP_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(WIDGETS_FILE_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(SEARCH_INDEX_DIR));

        fileRepository.updateProjectDirectory(projectRoot,
                List.of(
                        new FileModification("utils/Metashape/artivact-metashape-2.1.1-workflow.xml", "##EXPORT_PATH##",
                                projectRoot.resolve("temp/export/metashape-export.obj").toAbsolutePath().toString()),
                        new FileModification("utils/Meshroom/artivact-meshroom-workflow.mg", "##EXPORT_PATH##",
                                projectRoot.resolve("temp/export/").toAbsolutePath().toString().replace("\\", "/"))
                ),
                TEMP_DIR);
    }

}
