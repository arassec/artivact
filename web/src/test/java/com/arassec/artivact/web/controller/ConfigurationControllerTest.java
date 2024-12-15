package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.model.appearance.ColorTheme;
import com.arassec.artivact.core.model.appearance.License;
import com.arassec.artivact.core.model.configuration.*;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.web.model.ApplicationSettings;
import com.arassec.artivact.web.model.Profiles;
import com.arassec.artivact.web.model.UserData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ConfigurationController}.
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private ConfigurationController controller;

    /**
     * Service mock.
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * Tests getting the application settings.
     */
    @Test
    void testGetApplicationSettings() {
        ColorTheme colorTheme = new ColorTheme();
        License license = new License();

        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setApplicationTitle("title");
        appearanceConfiguration.setApplicationLocale("locale");
        appearanceConfiguration.setAvailableLocales("de,ja");
        appearanceConfiguration.setColorTheme(colorTheme);
        appearanceConfiguration.setLicense(license);
        appearanceConfiguration.setEncodedFavicon("encodedFavicon");

        when(configurationService.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        when(configurationService.isDesktopProfileEnabled()).thenReturn(true);
        when(configurationService.isE2eProfileEnabled()).thenReturn(true);

        when(configurationService.loadExchangeConfiguration()).thenReturn(new ExchangeConfiguration("remoteServer", "apiToken"));

        ApplicationSettings applicationSettings = controller.getApplicationSettings();

        Profiles profiles = new Profiles();
        profiles.setDesktop(true);
        profiles.setE2e(true);

        assertEquals("title", applicationSettings.getApplicationTitle());
        assertEquals("locale", applicationSettings.getApplicationLocale());
        assertEquals(List.of("de", "ja"), applicationSettings.getAvailableLocales());
        assertEquals(colorTheme, applicationSettings.getColorTheme());
        assertEquals(license, applicationSettings.getLicense());
        assertEquals("encodedFavicon", appearanceConfiguration.getEncodedFavicon());
        assertEquals(profiles, applicationSettings.getProfiles());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), applicationSettings.getAvailableRoles());
        assertTrue(applicationSettings.isSyncAvailable());
    }

    /**
     * Tests getting user details.
     */
    @Test
    void testGetUserData() {
        assertFalse(controller.getUserData(null).isAuthenticated());

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("username");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UserData userData = controller.getUserData(authentication);
        assertEquals("username", userData.getUsername());
        assertTrue(userData.isAuthenticated());
    }

    /**
     * tests retrieving properties.
     */
    @Test
    void testGetPublicPropertyCategories() {
        List<PropertyCategory> properties = new LinkedList<>();
        when(configurationService.loadTranslatedRestrictedProperties()).thenReturn(properties);
        assertEquals(properties, controller.getPublicPropertyCategories());
    }

    /**
     * Tests getting tags.
     */
    @Test
    void testGetPublicTagsConfiguration() {
        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        when(configurationService.loadTranslatedRestrictedTags()).thenReturn(tagsConfiguration);
        assertEquals(tagsConfiguration, controller.getPublicTagsConfiguration());
    }

    /**
     * Tests loading the application's favicon.
     */
    @Test
    void testGetFavicon() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setEncodedFavicon(Base64.getEncoder().encodeToString("large-favicon".getBytes()));

        when(configurationService.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        HttpEntity<byte[]> httpEntity = controller.getFavicon();
        assertEquals("large-favicon", new String(Objects.requireNonNull(httpEntity.getBody())));
        assertEquals(MediaType.valueOf("image/ico"), httpEntity.getHeaders().getContentType());
    }

    /**
     * Tests saving a favicon.
     */
    @Test
    @SneakyThrows
    void testUploadFavicon() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        when(configurationService.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getBytes()).thenReturn("favicon".getBytes());

        ResponseEntity<Void> responseEntity = controller.uploadFavicon(multipartFile);
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());

        ArgumentCaptor<AppearanceConfiguration> argCap = ArgumentCaptor.forClass(AppearanceConfiguration.class);
        verify(configurationService, times(1)).saveAppearanceConfiguration(argCap.capture());

        assertEquals(Base64.getEncoder().encodeToString("favicon".getBytes()), argCap.getValue().getEncodedFavicon());
    }

    /**
     * Tests loading the properties configuration.
     */
    @Test
    void testGetPropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        when(configurationService.loadPropertiesConfiguration()).thenReturn(propertiesConfiguration);
        assertEquals(propertiesConfiguration, controller.getPropertiesConfiguration());
    }

    /**
     * Tests exporting the current properties configuration.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfiguration() {
        when(configurationService.exportPropertiesConfiguration()).thenReturn("properties-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                controller.exportPropertiesConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
    }

    /**
     * Tests saving the properties configuration.
     */
    @Test
    void testSavePropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        controller.savePropertiesConfiguration(propertiesConfiguration);
        verify(configurationService, times(1)).savePropertiesConfiguration(propertiesConfiguration);
    }

    /**
     * Tests loading the appearance configuration.
     */
    @Test
    void testGetAppearanceConfiguration() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        when(configurationService.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);
        assertEquals(appearanceConfiguration, controller.getAppearanceConfiguration());
    }

    /**
     * Tests saving the appearance configuration.
     */
    @Test
    void testSaveAppearanceConfiguration() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        controller.saveAppearanceConfiguration(appearanceConfiguration);
        verify(configurationService, times(1)).saveAppearanceConfiguration(appearanceConfiguration);
    }

    /**
     * Tests getting the tags configuration.
     */
    @Test
    void testGetTagsConfiguration() {
        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        when(configurationService.loadTagsConfiguration()).thenReturn(tagsConfiguration);
        assertEquals(tagsConfiguration, controller.getTagsConfiguration());
    }

    /**
     * Tests saving the tags configuration.
     */
    @Test
    void testSaveTagsConfiguration() {
        TagsConfiguration tagsConfiguration = new TagsConfiguration();
        controller.saveTagsConfiguration(tagsConfiguration);
        verify(configurationService, times(1)).saveTagsConfiguration(tagsConfiguration);
    }

    /**
     * Tests the tag configuration export.
     */
    @Test
    @SneakyThrows
    void testExportTagsConfiguration() {
        when(configurationService.exportTagsConfiguration()).thenReturn("tags-configuration-json");

        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        ResponseEntity<StreamingResponseBody> streamingResponseBodyResponseEntity =
                controller.exportTagsConfiguration(httpServletResponse);

        assertEquals(HttpStatus.OK, streamingResponseBodyResponseEntity.getStatusCode());

        verify(httpServletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(httpServletResponse, times(1)).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), anyString());
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(httpServletResponse, times(1)).addHeader(HttpHeaders.EXPIRES, "0");
    }

    /**
     * Tests getting the adapter configuration.
     */
    @Test
    void testGetAdapterConfiguration() {
        AdapterConfiguration adapterConfiguration = new AdapterConfiguration();
        when(configurationService.loadAdapterConfiguration()).thenReturn(adapterConfiguration);
        assertEquals(adapterConfiguration, controller.getAdapterConfiguration());
    }

    /**
     * Tests saving the adapter configuration.
     */
    @Test
    void testSaveAdapterConfiguration() {
        AdapterConfiguration adapterConfiguration = new AdapterConfiguration();
        controller.saveAdapterConfiguration(adapterConfiguration);
        verify(configurationService, times(1)).saveAdapterConfiguration(adapterConfiguration);
    }

    /**
     * Tests getting the exchange configuration.
     */
    @Test
    void testGetExchangeConfiguration() {
        ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration();
        when(configurationService.loadExchangeConfiguration()).thenReturn(exchangeConfiguration);
        assertEquals(exchangeConfiguration, controller.getExchangeConfiguration());
    }

    /**
     * Tests saving the exchange configuration.
     */
    @Test
    void testSaveExchangeConfiguration() {
        ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration();
        controller.saveExchangeConfiguration(exchangeConfiguration);
        verify(configurationService, times(1)).saveExchangeConfiguration(exchangeConfiguration);
    }

}
