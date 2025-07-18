package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.account.CreateAccountUseCase;
import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.CheckRuntimeConfigurationUseCase;
import com.arassec.artivact.application.port.in.exchange.ImportContentUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.misc.FileModification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Service for setting the application up on initial start.
 */
@Slf4j
@Service
public class ProjectInitializationService {

    /**
     * Directory containing files to copy during project setup.
     */
    public static final Path PROJECT_SETUP_DIR = Path.of("resources/project-setup");

    /**
     * Directory containing files to copy during project setup if the regular directory does not exist.
     */
    public static final Path PROJECT_SETUP_DIR_FALLBACK = Path.of("application/src/main/resources/project-setup");

    /**
     * Placeholder for the export path in templates.
     */
    private static final String TPL_EXPORT_PATH_PLACEHOLDER = "##EXPORT_PATH##";

    /**
     * The initial user's username.
     */
    private static final String INITIAL_USERNAME = "admin";

    /**
     * Path to the welcome page export file.
     */
    private static final String WELCOME_EXPORT_PATH = "utils/Setup/welcome-page.artivact.content.zip";

    private final UseProjectDirsUseCase useProjectDirsUseCase;

    private final LoadAccountUseCase loadAccountUseCase;

    private final CreateAccountUseCase createAccountUseCase;

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * Repository for file access.
     */
    private final FileRepository fileRepository;

    /**
     * Artivact importer, used to import the welcome page.
     */
    private final ImportContentUseCase importContentUseCase;

    private final CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

    /**
     * Initial administrator password. Can be set per JVM parameter for integration testing.
     */
    private final String initialPassword;

    public ProjectInitializationService(UseProjectDirsUseCase useProjectDirsUseCase,
                                        LoadAccountUseCase loadAccountUseCase,
                                        CreateAccountUseCase createAccountUseCase,
                                        PageRepository pageRepository,
                                        FileRepository fileRepository,
                                        ImportContentUseCase importContentUseCase,
                                        CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase,
                                        @Value("${artivact.initial.password:}") String initialPassword) {
        this.useProjectDirsUseCase = useProjectDirsUseCase;
        this.loadAccountUseCase = loadAccountUseCase;
        this.createAccountUseCase = createAccountUseCase;
        this.pageRepository = pageRepository;
        this.fileRepository = fileRepository;
        this.importContentUseCase = importContentUseCase;
        this.checkRuntimeConfigurationUseCase = checkRuntimeConfigurationUseCase;
        this.initialPassword = initialPassword;
    }

    /**
     * Initializes the application by creating an initial user account if none is available. The password is printed
     * to the application's log.
     */
    @PostConstruct
    public void initialize() {
        initializeProjectDir();
        initializeAdminAccount();
        importWelcomePage();
    }

    /**
     * Updates the project's filesystem structure.
     */
    private void initializeProjectDir() {
        Path projectRoot = useProjectDirsUseCase.getProjectRoot();

        fileRepository.createDirIfRequired(projectRoot.resolve(DirectoryDefinitions.ITEMS_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(DirectoryDefinitions.EXPORT_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(DirectoryDefinitions.TEMP_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(DirectoryDefinitions.WIDGETS_DIR));
        fileRepository.createDirIfRequired(projectRoot.resolve(DirectoryDefinitions.SEARCH_INDEX_DIR));

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

    /**
     * Initializes an admin account and logs the account's password to the application's logfile.
     */
    @SuppressWarnings("LoggingSimilarMessage")
    private void initializeAdminAccount() {
        if (loadAccountUseCase.loadAll().isEmpty()) {

            String password = initialPassword;
            if (!StringUtils.hasText(initialPassword)) {
                password = UUID.randomUUID().toString().split("-")[0];
            }

            createAccountUseCase.create(Account.builder()
                    .username(INITIAL_USERNAME)
                    .password(password)
                    .user(true)
                    .admin(true)
                    .build());

            log.info("");
            log.info("##############################################################");
            log.info("Initial user created: {} / {}", INITIAL_USERNAME, initialPassword);
            log.info("##############################################################");
            log.info("");
        }
    }

    /**
     * Imports the welcome page if no other pages exist.
     */
    private void importWelcomePage() {
        if (pageRepository.findAll().isEmpty() &&
                (checkRuntimeConfigurationUseCase.isDesktopProfileEnabled() || checkRuntimeConfigurationUseCase.isE2eProfileEnabled())) {
            Path welcomePageExportZip = useProjectDirsUseCase.getProjectRoot().resolve(WELCOME_EXPORT_PATH);

            if (!fileRepository.exists(welcomePageExportZip)) {
                welcomePageExportZip = PROJECT_SETUP_DIR_FALLBACK.resolve(WELCOME_EXPORT_PATH);
            }

            if (fileRepository.exists(welcomePageExportZip)) {
                importContentUseCase.importContent(welcomePageExportZip);
                log.info("Welcome page imported!");
            } else {
                log.info("Welcome page import not found!");
            }
        }
    }

}


