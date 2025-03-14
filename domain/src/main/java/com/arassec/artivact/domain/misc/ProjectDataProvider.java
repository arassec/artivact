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
    public static final String WIDGETS_DIR = "widgets";

    /**
     * Directory containing additional data (like files) from menus.
     */
    public static final String MENUS_DIR = "menus";

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
     * Directory containing files to copy during project setup.
     */
    public static final Path PROJECT_SETUP_DIR = Path.of("resources/project-setup");

    /**
     * Directory containing files to copy during project setup if the regular directory does not exist.
     */
    public static final Path PROJECT_SETUP_DIR_FALLBACK = Path.of("domain/src/main/resources/project-setup");

    /**
     * Placeholder for the export path in templates.
     */
    private static final String TPL_EXPORT_PATH_PLACEHOLDER = "##EXPORT_PATH##";

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
        fileRepository.createDirIfRequired(projectRoot.resolve(WIDGETS_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(SEARCH_INDEX_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(MENUS_DIR));

        fileRepository.updateProjectDirectory(projectRoot, PROJECT_SETUP_DIR, PROJECT_SETUP_DIR_FALLBACK,
                List.of(
                        new FileModification("utils/Metashape/artivact-metashape-2.1-workflow.xml", TPL_EXPORT_PATH_PLACEHOLDER,
                                projectRoot.resolve("temp/export/metashape-export.obj").toAbsolutePath().toString()),
                        new FileModification("utils/Metashape/artivact-metashape-2.2-workflow.xml", TPL_EXPORT_PATH_PLACEHOLDER,
                                projectRoot.resolve("temp/export/metashape-export.obj").toAbsolutePath().toString()),
                        new FileModification("utils/Meshroom/artivact-meshroom-workflow.mg", TPL_EXPORT_PATH_PLACEHOLDER,
                                projectRoot.resolve("temp/export/").toAbsolutePath().toString().replace("\\", "/"))
                )
        );
    }

}
