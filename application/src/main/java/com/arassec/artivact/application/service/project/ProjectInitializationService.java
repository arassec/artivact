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
     * The initial user's username.
     */
    private static final String INITIAL_USERNAME = "admin";

    private static final String INITIAL_INDEX_PAGE_ID = "8b2ae4ac-51cb-4de0-b2ee-ad531e01b176";

    /**
     * Path to the welcome page export file.
     */
    private static final String WELCOME_EXPORT_PATH = "utils/Setup/welcome.artivact.collection.zip";

    /**
     * Use case for use project dirs.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for load account.
     */
    private final LoadAccountUseCase loadAccountUseCase;

    /**
     * Use case for create account.
     */
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

    /**
     * Use case for check runtime configuration.
     */
    private final CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

    /**
     * Repository for configuration.
     */
    private final ConfigurationRepository configurationRepository;

    /**
     * Initial administrator password. Can be set per JVM parameter for integration testing.
     */
    private final String initialPassword;

    /**
     * Creates a new ProjectInitializationService with the provided dependencies.
     *
     * @param useProjectDirsUseCase Use case for project directories.
     * @param loadAccountUseCase Use case for loading accounts.
     * @param createAccountUseCase Use case for creating accounts.
     * @param pageRepository Repository for pages.
     * @param fileRepository Repository for files.
     * @param importMenuUseCase Use case for importing menus.
     * @param checkRuntimeConfigurationUseCase Use case for checking runtime configuration.
     * @param configurationRepository Repository for configuration.
     * @param initialPassword The initial administrator password.
     */
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

        fileRepository.updateProjectDirectory(projectRoot, PROJECT_SETUP_DIR, PROJECT_SETUP_DIR_FALLBACK);
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
            log.info("Initial user created: {} / {}", INITIAL_USERNAME, password);
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
            if (checkRuntimeConfigurationUseCase.isDesktopProfileEnabled() || checkRuntimeConfigurationUseCase.isE2eProfileEnabled()) {
                appearanceConfiguration.setIndexPageId(INITIAL_INDEX_PAGE_ID);
            }
            if (checkRuntimeConfigurationUseCase.isE2eProfileEnabled()) {
                appearanceConfiguration.setAvailableLocales("de");
            } else {
                appearanceConfiguration.setAvailableLocales("");
            }
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


