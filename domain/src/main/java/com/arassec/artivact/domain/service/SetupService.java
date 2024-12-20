package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.model.account.Account;
import com.arassec.artivact.core.model.configuration.ConfigurationType;
import com.arassec.artivact.core.model.configuration.MenuConfiguration;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.repository.AccountRepository;
import com.arassec.artivact.core.repository.ConfigurationRepository;
import com.arassec.artivact.core.repository.PageRepository;
import com.arassec.artivact.domain.exchange.ArtivactImporter;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for setting the application up on initial start.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class SetupService {

    /**
     * The initial user's username.
     */
    private static final String INITIAL_USERNAME = "admin";

    /**
     * Path to the welcome page export file.
     */
    private static final String WELCOME_EXPORT_PATH = "utils/Setup/welcome-page.artivact.content.zip";

    /**
     * Repository for accounts.
     */
    private final AccountRepository accountRepository;

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * Service for account handling.
     */
    private final AccountService accountService;

    /**
     * Artivact importer, used to import the welcome page.
     */
    private final ArtivactImporter artivactImporter;

    /**
     * The project data provider.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The configuration service.
     */
    private final ConfigurationService configurationService;

    /**
     * The configuration repository.
     */
    private final ConfigurationRepository configurationRepository;

    /**
     * Service for menus.
     */
    private final MenuService menuService;

    /**
     * Initial administrator password. Can be set per JVM parameter for integration testing.
     */
    @Value("${artivact.initial.password:}")
    private String initialPassword;

    /**
     * Initializes the application by creating an initial user account if none is available. The password is printed
     * to the application's log.
     */
    @SuppressWarnings("LoggingSimilarMessage")
    @PostConstruct
    public void initialize() {
        initializeAdminAccount();
        migrateMenus();
        importWelcomePage();
    }

    /**
     * Initializes an admin account and logs the account's password to the application's logfile.
     */
    private void initializeAdminAccount() {
        if (accountRepository.findAll().isEmpty()) {

            if (!StringUtils.hasText(initialPassword)) {
                initialPassword = UUID.randomUUID().toString().split("-")[0];
            }

            accountService.create(Account.builder()
                    .username(INITIAL_USERNAME)
                    .password(initialPassword)
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
     * Migrates menus from the configuration table to the menus table.
     */
    private void migrateMenus() {
        Optional<MenuConfiguration> menuConfigurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        if (menuConfigurationOptional.isPresent()) {
            List<Menu> menus = menuService.loadTranslatedRestrictedMenus();
            if (menus.isEmpty()) {
                MenuConfiguration menuConfiguration = menuConfigurationOptional.get();
                menuService.saveMenus(menuConfiguration.getMenus());
                log.info("Migrated menus!");
            }
        }
    }

    /**
     * Imports the welcome page if no other pages exist.
     */
    private void importWelcomePage() {
        if (pageRepository.findAll().isEmpty() &&
                (configurationService.isDesktopProfileEnabled() || configurationService.isE2eProfileEnabled())) {
            Path welcomePageExportZip = projectDataProvider.getProjectRoot()
                    .resolve(WELCOME_EXPORT_PATH);

            if (!Files.exists(welcomePageExportZip)) {
                welcomePageExportZip = ProjectDataProvider.PROJECT_SETUP_DIR_FALLBACK
                        .resolve(WELCOME_EXPORT_PATH);
            }

            if (Files.exists(welcomePageExportZip)) {
                artivactImporter.importContent(welcomePageExportZip);
                log.info("Welcome page imported!");
            } else {
                log.info("Welcome page import not found!");
            }
        }
    }

}


