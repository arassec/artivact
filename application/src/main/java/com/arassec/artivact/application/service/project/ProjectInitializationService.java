package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.account.CreateAccountUseCase;
import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.CheckRuntimeConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.appearance.ColorTheme;
import com.arassec.artivact.domain.model.appearance.License;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.FileModification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
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

    private static final String INITIAL_INDEX_PAGE_ID = "8b2ae4ac-51cb-4de0-b2ee-ad531e01b176";

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
    private final ImportMenuUseCase importMenuUseCase;

    private final CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

    private final ConfigurationRepository configurationRepository;

    /**
     * Initial administrator password. Can be set per JVM parameter for integration testing.
     */
    private final String initialPassword;

    public ProjectInitializationService(UseProjectDirsUseCase useProjectDirsUseCase,
                                        LoadAccountUseCase loadAccountUseCase,
                                        CreateAccountUseCase createAccountUseCase,
                                        PageRepository pageRepository,
                                        FileRepository fileRepository,
                                        ImportMenuUseCase importMenuUseCase,
                                        CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase,
                                        ConfigurationRepository configurationRepository,
                                        @Value("${artivact.initial.password:}") String initialPassword) {
        this.useProjectDirsUseCase = useProjectDirsUseCase;
        this.loadAccountUseCase = loadAccountUseCase;
        this.createAccountUseCase = createAccountUseCase;
        this.pageRepository = pageRepository;
        this.fileRepository = fileRepository;
        this.importMenuUseCase = importMenuUseCase;
        this.checkRuntimeConfigurationUseCase = checkRuntimeConfigurationUseCase;
        this.configurationRepository = configurationRepository;
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
        initializeAppearanceConfiguration();
        initializePeripheralConfiguration();
        initializeWelcomePage();
    }

    /**
     * Updates the project's filesystem structure.
     */
    private void initializeProjectDir() {
        Path projectRoot = useProjectDirsUseCase.getProjectRoot();

        fileRepository.createDirIfRequired(useProjectDirsUseCase.getItemsDir());
        fileRepository.createDirIfRequired(useProjectDirsUseCase.getExportsDir());
        fileRepository.createDirIfRequired(useProjectDirsUseCase.getTempDir());
        fileRepository.createDirIfRequired(useProjectDirsUseCase.getWidgetsDir());
        fileRepository.createDirIfRequired(useProjectDirsUseCase.getSearchIndexDir());

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
     * Initializes the application's appearance configuration.
     */
    private void initializeAppearanceConfiguration() {
        Optional<AppearanceConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class);

        if (configurationOptional.isEmpty()) {
            AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();

            appearanceConfiguration.setApplicationTitle("Artivact");
            appearanceConfiguration.setIndexPageId(INITIAL_INDEX_PAGE_ID);
            appearanceConfiguration.setAvailableLocales("");
            appearanceConfiguration.setLicense(new License());

            ColorTheme colorTheme = new ColorTheme();
            colorTheme.setPrimary("#6e7e85");
            colorTheme.setSecondary("#bbbac6");
            colorTheme.setAccent("#F5F5F5");
            colorTheme.setDark("#364958");
            colorTheme.setPositive("#87a330");
            colorTheme.setNegative("#a4031f");
            colorTheme.setInfo("#e2e2e2");
            colorTheme.setWarning("#e6c229");
            appearanceConfiguration.setColorTheme(colorTheme);

            ClassPathResource classPathResource = new ClassPathResource("icons/favicon-32x32.ico", this.getClass().getClassLoader());
            try (InputStream is = classPathResource.getInputStream()) {
                appearanceConfiguration.setEncodedFavicon(Base64.getEncoder().encodeToString(is.readAllBytes()));
            } catch (IOException e) {
                throw new ArtivactException("Could not read 32x32 pixel favicon!", e);
            }

            configurationRepository.saveConfiguration(ConfigurationType.APPEARANCE, appearanceConfiguration);
        }
    }

    /**
     * Initializes the peripheral configuration.
     */
    private void initializePeripheralConfiguration() {
        Optional<PeripheralConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.PERIPHERAL, PeripheralConfiguration.class);

        if (configurationOptional.isEmpty()) {
            boolean windowsOs = System.getProperty("os.name").toLowerCase().contains("windows");

            //noinspection ExtractMethodRecommender
            PeripheralConfiguration peripheralConfiguration = new PeripheralConfiguration();

            peripheralConfiguration.setImageManipulationPeripheralImplementation(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);
            peripheralConfiguration.setCameraPeripheralImplementation(PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL);
            peripheralConfiguration.setTurntablePeripheralImplementation(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL);
            peripheralConfiguration.setModelCreatorPeripheralImplementation(PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL);
            peripheralConfiguration.setModelEditorPeripheralImplementation(PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL);

            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL, "100");

            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL, "silueta.onnx#input.1#320#320#5");

            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL, "");
            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL, "C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe");
            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL, "/usr/bin/gphoto2");

            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL, "");
            peripheralConfiguration.getConfigValues().put(PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL, "");

            if (windowsOs) {
                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL, "C:/Users/<USER>/Tools/Meshroom/Meshroom.exe");
                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL, "C:/Program Files/Agisoft/Metashape/metashape.exe");
                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL, "C:/Program Files/Capturing Reality/RealityScan/RealityScan.exe");

                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL, "C:/Users/<USER>/Tools/Blender/blender.exe");
            } else {
                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL, "/home/<USER>/Tools/meshroom/Meshroom");
                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL, "/home/<USER>/Tools/metashape/metashape.sh");

                peripheralConfiguration.getConfigValues().put(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL, "/home/<USER>/Tools/blender/blender");
            }

            configurationRepository.saveConfiguration(ConfigurationType.PERIPHERAL, peripheralConfiguration);
        }
    }

    /**
     * Imports the welcome page if no other pages exist.
     */
    private void initializeWelcomePage() {
        if (pageRepository.findAll().isEmpty() &&
                (checkRuntimeConfigurationUseCase.isDesktopProfileEnabled() || checkRuntimeConfigurationUseCase.isE2eProfileEnabled())) {
            Path welcomePageExportZip = useProjectDirsUseCase.getProjectRoot().resolve(WELCOME_EXPORT_PATH);

            if (!fileRepository.exists(welcomePageExportZip)) {
                welcomePageExportZip = PROJECT_SETUP_DIR_FALLBACK.resolve(WELCOME_EXPORT_PATH);
            }

            if (fileRepository.exists(welcomePageExportZip)) {
                importMenuUseCase.importMenu(welcomePageExportZip);

                AppearanceConfiguration appearanceConfiguration = configurationRepository
                        .findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class).orElse(new AppearanceConfiguration());
                configurationRepository.saveConfiguration(ConfigurationType.APPEARANCE, appearanceConfiguration);

                log.info("Welcome page imported!");
            } else {
                log.info("Welcome page import not found!");
            }
        }
    }

}


