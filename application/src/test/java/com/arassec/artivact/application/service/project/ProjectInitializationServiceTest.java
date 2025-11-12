package com.arassec.artivact.application.service.project;

import com.arassec.artivact.application.port.in.account.CreateAccountUseCase;
import com.arassec.artivact.application.port.in.account.LoadAccountUseCase;
import com.arassec.artivact.application.port.in.configuration.CheckRuntimeConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.ImportMenuUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.model.account.Account;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectInitializationServiceTest {

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadAccountUseCase loadAccountUseCase;

    @Mock
    private CreateAccountUseCase createAccountUseCase;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ImportMenuUseCase importMenuUseCase;

    @Mock
    private CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

    @Mock
    private ConfigurationRepository configurationRepository;

    @InjectMocks
    private ProjectInitializationService service;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private Path projectRoot;

    @BeforeEach
    void setUp() {
        projectRoot = Path.of("projectRoot");
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(projectRoot);
        when(useProjectDirsUseCase.getItemsDir()).thenReturn(projectRoot.resolve("items"));
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(projectRoot.resolve("exports"));
        when(useProjectDirsUseCase.getTempDir()).thenReturn(projectRoot.resolve("temp"));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(projectRoot.resolve("widgets"));
        when(useProjectDirsUseCase.getSearchIndexDir()).thenReturn(projectRoot.resolve("searchIndex"));
    }

    @Test
    void testInitializeAdminAccountCreatesAccountIfNoneExists() {
        when(loadAccountUseCase.loadAll()).thenReturn(Collections.emptyList());

        service.initialize();

        verify(createAccountUseCase).create(accountCaptor.capture());
        Account createdAccount = accountCaptor.getValue();
        assertThat(createdAccount.getUsername()).isEqualTo("admin");
        assertThat(createdAccount.getAdmin()).isTrue();
        assertThat(createdAccount.getUser()).isTrue();
        assertThat(StringUtils.hasText(createdAccount.getPassword())).isTrue();
    }

    @Test
    void testInitializeProjectDirCreatesRequiredDirsAndUpdatesProjectDirectory() {
        service.initialize();

        verify(fileRepository).createDirIfRequired(projectRoot.resolve("items"));
        verify(fileRepository).createDirIfRequired(projectRoot.resolve("exports"));
        verify(fileRepository).createDirIfRequired(projectRoot.resolve("temp"));
        verify(fileRepository).createDirIfRequired(projectRoot.resolve("widgets"));
        verify(fileRepository).createDirIfRequired(projectRoot.resolve("searchIndex"));

        verify(fileRepository).updateProjectDirectory(eq(projectRoot), any(), any());
    }

    @Test
    void testInitializeAppearanceConfigurationCreatesDefaultConfiguration() {
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

        service.initialize();

        verify(configurationRepository).saveConfiguration(eq(ConfigurationType.APPEARANCE), any(AppearanceConfiguration.class));
    }

    @Test
    void testInitializeWelcomePageUsesFallbackIfNotExistsInProjectRoot() {
        when(checkRuntimeConfigurationUseCase.isDesktopProfileEnabled()).thenReturn(true);

        when(loadAccountUseCase.loadAll()).thenReturn(Collections.emptyList());
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());
        when(pageRepository.findAll()).thenReturn(Collections.emptyList());

        // Simulate welcome page missing in projectRoot but exists in fallback
        when(fileRepository.exists(projectRoot.resolve("utils/Setup/welcome.artivact.collection.zip"))).thenReturn(false);
        when(fileRepository.exists(ProjectInitializationService.PROJECT_SETUP_DIR_FALLBACK.resolve("utils/Setup/welcome.artivact.collection.zip"))).thenReturn(true);

        service.initialize();

        verify(importMenuUseCase).importMenu(ProjectInitializationService.PROJECT_SETUP_DIR_FALLBACK.resolve("utils/Setup/welcome.artivact.collection.zip"));
    }

    @Test
    void testInitializeWelcomePageDoesNothingIfPageAlreadyExists() {
        when(pageRepository.findAll()).thenReturn(List.of(mock(Page.class)));

        service.initialize();

        verify(importMenuUseCase, never()).importMenu(any());
    }

}
