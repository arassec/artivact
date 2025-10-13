package com.arassec.artivact.adapter.in.rest.controller.configuration;

import com.arassec.artivact.adapter.in.rest.model.ApplicationSettings;
import com.arassec.artivact.adapter.in.rest.model.UserData;
import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.project.CleanupExportFilesUseCase;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.appearance.ColorTheme;
import com.arassec.artivact.domain.model.appearance.License;
import com.arassec.artivact.domain.model.configuration.*;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationControllerTest {

    @Mock
    private LoadPropertiesConfigurationUseCase loadPropertiesConfigurationUseCase;

    @Mock
    private SavePropertiesConfigurationUseCase savePropertiesConfigurationUseCase;

    @Mock
    private ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

    @Mock
    private ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

    @Mock
    private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    @Mock
    private LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

    @Mock
    private CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

    @Mock
    private LoadTagsConfigurationUseCase loadTagsConfigurationUseCase;

    @Mock
    private SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

    @Mock
    private SaveTagsConfigurationUseCase saveTagsConfigurationUseCase;

    @Mock
    private SaveExchangeConfigurationUseCase saveExchangeConfigurationUseCase;

    @Mock
    private SavePeripheralConfigurationUseCase savePeripheralConfigurationUseCase;

    @Mock
    private LoadPeripheralConfigurationUseCase loadPeripheralConfigurationUseCase;

    @Mock
    private CleanupExportFilesUseCase cleanupExportFilesUseCase;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    @InjectMocks
    private ConfigurationController controller;

    @Test
    void testGetApplicationSettings() {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        ac.setApplicationTitle("MyApp");
        ac.setAvailableLocales("en,de");
        ac.setApplicationLocale("en");
        ac.setColorTheme(new ColorTheme());
        ac.setLicense(new License());
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(ac);
        ExchangeConfiguration ec = new ExchangeConfiguration();
        ec.setRemoteServer("server");
        ec.setApiToken("token");
        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(ec);
        when(checkRuntimeConfigurationUseCase.isDesktopProfileEnabled()).thenReturn(true);
        when(checkRuntimeConfigurationUseCase.isE2eProfileEnabled()).thenReturn(false);

        ApplicationSettings result = controller.getApplicationSettings();

        assertThat(result.getApplicationTitle()).isEqualTo("MyApp");
        assertThat(result.getAvailableLocales()).contains("en", "de");
        assertThat(result.getProfiles().isDesktop()).isTrue();
        assertThat(result.getAvailableRoles()).contains(Roles.ROLE_ADMIN, Roles.ROLE_USER);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testGetUserDataAuthenticated() {
        GrantedAuthority grantedAuthority = mock(GrantedAuthority.class);
        when(grantedAuthority.getAuthority()).thenReturn("ROLE_ADMIN");

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((Collection) List.of(grantedAuthority));
        when(userDetails.getUsername()).thenReturn("john");

        UserData data = controller.getUserData(auth);

        assertThat(data.isAuthenticated()).isTrue();
        assertThat(data.getRoles()).contains("ROLE_ADMIN");
        assertThat(data.getUsername()).isEqualTo("john");
    }

    @Test
    void testGetUserDataUnauthenticated() {
        UserData data = controller.getUserData(null);
        assertThat(data.isAuthenticated()).isFalse();
    }

    @Test
    void testGetPublicPropertyCategories() {
        PropertyCategory category = new PropertyCategory();
        when(loadPropertiesConfigurationUseCase.loadTranslatedRestrictedProperties()).thenReturn(List.of(category));
        List<PropertyCategory> result = controller.getPublicPropertyCategories();
        assertThat(result).contains(category);
    }

    @Test
    void testGetPublicTagsConfiguration() {
        TagsConfiguration tc = new TagsConfiguration();
        when(loadTagsConfigurationUseCase.loadTranslatedRestrictedTagsConfiguration()).thenReturn(tc);
        assertThat(controller.getPublicTagsConfiguration()).isEqualTo(tc);
    }

    @Test
    void testGetFavicon() {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        ac.setEncodedFavicon(java.util.Base64.getEncoder().encodeToString("ico".getBytes()));
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(ac);

        HttpEntity<byte[]> result = controller.getFavicon();
        assertThat(result.getBody()).isNotNull();
        assertThat(new String(result.getBody())).isEqualTo("ico");
    }

    @Test
    void testUploadFavicon() {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(ac);
        MockMultipartFile file = new MockMultipartFile("file", "favicon.ico", "image/x-icon", "ico".getBytes());

        ResponseEntity<Void> result = controller.uploadFavicon(file);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        verify(saveAppearanceConfigurationUseCase).saveAppearanceConfiguration(ac);
    }

    @Test
    void testUploadFaviconIOException() throws Exception {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(ac);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException());

        assertThatThrownBy(() -> controller.uploadFavicon(file)).isInstanceOf(ArtivactException.class);
    }

    @Test
    void testGetPropertiesConfiguration() {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        when(loadPropertiesConfigurationUseCase.loadPropertiesConfiguration()).thenReturn(pc);
        assertThat(controller.getPropertiesConfiguration()).isEqualTo(pc);
    }

    @Test
    void testSavePropertiesConfiguration() {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        controller.savePropertiesConfiguration(pc);
        verify(savePropertiesConfigurationUseCase).savePropertiesConfiguration(pc);
    }

    @Test
    void testExportPropertiesConfiguration() throws Exception {
        when(exportPropertiesConfigurationUseCase.exportPropertiesConfiguration()).thenReturn("json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);

        ResponseEntity<StreamingResponseBody> result = controller.exportPropertiesConfiguration(response);

        assertThat(result.getBody()).isNotNull();
        result.getBody().writeTo(new ByteArrayOutputStream());
        verify(cleanupExportFilesUseCase).cleanupPropertiesConfigurationExport();
    }

    @Test
    void testGetAppearanceConfiguration() {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(ac);
        assertThat(controller.getAppearanceConfiguration()).isEqualTo(ac);
    }

    @Test
    void testSaveAppearanceConfiguration() {
        AppearanceConfiguration ac = new AppearanceConfiguration();
        controller.saveAppearanceConfiguration(ac);
        verify(saveAppearanceConfigurationUseCase).saveAppearanceConfiguration(ac);
    }

    @Test
    void testGetTagsConfiguration() {
        TagsConfiguration tc = new TagsConfiguration();
        when(loadTagsConfigurationUseCase.loadTagsConfiguration()).thenReturn(tc);
        assertThat(controller.getTagsConfiguration()).isEqualTo(tc);
    }

    @Test
    void testSaveTagsConfiguration() {
        TagsConfiguration tc = new TagsConfiguration();
        controller.saveTagsConfiguration(tc);
        verify(saveTagsConfigurationUseCase).saveTagsConfiguration(tc);
    }

    @Test
    void testExportTagsConfiguration() throws Exception {
        when(exportTagsConfigurationUseCase.exportTagsConfiguration()).thenReturn("json");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);

        ResponseEntity<StreamingResponseBody> result = controller.exportTagsConfiguration(response);
        assertThat(result.getBody()).isNotNull();
        result.getBody().writeTo(new ByteArrayOutputStream());
        verify(cleanupExportFilesUseCase).cleanupTagsConfigurationExport();
    }

    @Test
    void testGetPeripheralsConfiguration() {
        PeripheralsConfiguration pc = new PeripheralsConfiguration();
        when(loadPeripheralConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(pc);
        assertThat(controller.getPeripheralsConfiguration()).isEqualTo(pc);
    }

    @Test
    void testSavePeripheralsConfiguration() {
        PeripheralsConfiguration pc = new PeripheralsConfiguration();
        controller.savePeripheralsConfiguration(pc);
        verify(savePeripheralConfigurationUseCase).savePeripheralConfiguration(pc);
    }

    @Test
    void testGetExchangeConfiguration() {
        ExchangeConfiguration ec = new ExchangeConfiguration();
        when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(ec);
        assertThat(controller.getExchangeConfiguration()).isEqualTo(ec);
    }

    @Test
    void testSaveExchangeConfiguration() {
        ExchangeConfiguration ec = new ExchangeConfiguration();
        controller.saveExchangeConfiguration(ec);
        verify(saveExchangeConfigurationUseCase).saveExchangeConfiguration(ec);
    }

    @Test
    void testImportPropertiesConfiguration() {
        MockMultipartFile file = new MockMultipartFile("file", "props.json", "application/json", "json".getBytes());
        ResponseEntity<String> result = controller.importPropertiesConfiguration(file);
        assertThat(result.getBody()).isEqualTo("Properties imported.");
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration("json");
    }

    @Test
    void testImportPropertiesConfigurationIOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException());
        assertThatThrownBy(() -> controller.importPropertiesConfiguration(file)).isInstanceOf(ArtivactException.class);
    }

    @Test
    void testImportTagsConfiguration() {
        MockMultipartFile file = new MockMultipartFile("file", "tags.json", "application/json", "json".getBytes());
        ResponseEntity<String> result = controller.importTagsConfiguration(file);
        assertThat(result.getBody()).isEqualTo("Tags imported.");
        verify(importTagsConfigurationUseCase).importTagsConfiguration("json");
    }

    @Test
    void testImportTagsConfigurationIOException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException());
        assertThatThrownBy(() -> controller.importTagsConfiguration(file)).isInstanceOf(ArtivactException.class);
    }

}
