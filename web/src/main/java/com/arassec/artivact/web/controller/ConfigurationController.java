package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.configuration.*;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.domain.exchange.ExchangeDefinitions;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.web.model.ApplicationSettings;
import com.arassec.artivact.web.model.Profiles;
import com.arassec.artivact.web.model.UserData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * REST-Controller for configuration handling.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController extends BaseController {

    /**
     * The application's {@link ConfigurationService}.
     */
    private final ConfigurationService configurationService;

    /**
     * Returns the current appearance configuration.
     *
     * @return The {@link AppearanceConfiguration} of the app.
     */
    @GetMapping(value = "/public/settings")
    public ApplicationSettings getApplicationSettings() {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadTranslatedAppearanceConfiguration();

        ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setApplicationTitle(appearanceConfiguration.getApplicationTitle());

        String availableLocales = appearanceConfiguration.getAvailableLocales();
        if (StringUtils.hasText(availableLocales)) {
            applicationSettings.setAvailableLocales(Arrays.stream(availableLocales.split(",")).map(String::trim).toList());
        } else {
            applicationSettings.setAvailableLocales(List.of());
        }

        applicationSettings.setApplicationLocale(appearanceConfiguration.getApplicationLocale());
        applicationSettings.setColorTheme(appearanceConfiguration.getColorTheme());
        applicationSettings.setLicense(appearanceConfiguration.getLicense());
        applicationSettings.setProfiles(
                new Profiles(configurationService.isDesktopProfileEnabled(), configurationService.isE2eProfileEnabled()));
        applicationSettings.setAvailableRoles(List.of(Roles.ROLE_ADMIN, Roles.ROLE_USER));

        ExchangeConfiguration exchangeConfiguration = configurationService.loadExchangeConfiguration();
        applicationSettings.setSyncAvailable(StringUtils.hasText(exchangeConfiguration.getRemoteServer())
                && StringUtils.hasText(exchangeConfiguration.getApiToken()));

        return applicationSettings;
    }

    /**
     * Returns data about the current user.
     *
     * @param authentication Spring-Security's {@link Authentication} of the current user.
     * @return User data of the current user.
     */
    @GetMapping(value = "/public/user")
    public UserData getUserData(Authentication authentication) {
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return UserData.builder()
                    .authenticated(true)
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .username(userDetails.getUsername())
                    .build();
        }
        return UserData.builder().authenticated(false).build();
    }

    /**
     * Returns the configured properties in their categories.
     *
     * @return The categories containing item properties.
     */
    @GetMapping(value = "/public/property")
    public List<PropertyCategory> getPublicPropertyCategories() {
        return configurationService.loadTranslatedRestrictedProperties();
    }

    /**
     * Returns the current tag configuration.
     *
     * @return The tag configuration.
     */
    @GetMapping(value = "/public/tag")
    public TagsConfiguration getPublicTagsConfiguration() {
        return configurationService.loadTranslatedRestrictedTags();
    }

    /**
     * Returns the application's favicon in the requested size.
     *
     * @return The favicon as a byte array.
     */
    @GetMapping(value = "/public/favicon", produces = "image/x-icon")
    public HttpEntity<byte[]> getFavicon() {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadTranslatedAppearanceConfiguration();

        String base64EncodedFavicon = appearanceConfiguration.getEncodedFavicon();

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "favicon.ico");
        headers.add(HttpHeaders.PRAGMA, NO_CACHE);
        headers.add(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);
        headers.setContentType(MediaType.valueOf("image/x-icon"));

        return new HttpEntity<>(Base64.getDecoder().decode(base64EncodedFavicon), headers);
    }

    /**
     * Saves a favicon.
     *
     * @param file The new favicon to save.
     * @return HTTP-Status.
     */
    @PostMapping("/favicon")
    public ResponseEntity<Void> uploadFavicon(@RequestPart(value = "file") final MultipartFile file) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadTranslatedAppearanceConfiguration();
        try {
            appearanceConfiguration.setEncodedFavicon(Base64.getEncoder().encodeToString(file.getBytes()));
            configurationService.saveAppearanceConfiguration(appearanceConfiguration);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ArtivactException("Could not Base64 encode favicon!", e);
        }
    }

    /**
     * Returns the property configuration.
     *
     * @return The current property configuration.
     */
    @GetMapping(value = "/property")
    public PropertiesConfiguration getPropertiesConfiguration() {
        return configurationService.loadPropertiesConfiguration();
    }

    /**
     * Saves the property configuration
     *
     * @param propertiesConfiguration The configuration to save.
     */
    @PostMapping(value = "/property")
    public void savePropertiesConfiguration(@RequestBody PropertiesConfiguration propertiesConfiguration) {
        configurationService.savePropertiesConfiguration(propertiesConfiguration);
    }

    /**
     * Exports the current properties configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/property/export")
    public ResponseEntity<StreamingResponseBody> exportPropertiesConfiguration(HttpServletResponse response) {

        String exportedPropertiesConfiguration = configurationService.exportPropertiesConfiguration();

        StreamingResponseBody streamResponseBody = out -> {
            response.getOutputStream().write(exportedPropertiesConfiguration.getBytes());
            response.setContentLength(exportedPropertiesConfiguration.getBytes().length);
            configurationService.cleanupPropertiesConfigurationExport();
        };

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Returns the appearance configuration.
     *
     * @return The current appearance configuration.
     */
    @GetMapping(value = "/appearance")
    public AppearanceConfiguration getAppearanceConfiguration() {
        return configurationService.loadTranslatedAppearanceConfiguration();
    }

    /**
     * Saves the given appearance configuration.
     *
     * @param appearanceConfiguration The configuration to save.
     */
    @PostMapping(value = "/appearance")
    public void saveAppearanceConfiguration(@RequestBody AppearanceConfiguration appearanceConfiguration) {
        configurationService.saveAppearanceConfiguration(appearanceConfiguration);
    }

    /**
     * Returns the tag configuration.
     *
     * @return The current tag configuration.
     */
    @GetMapping(value = "/tags")
    public TagsConfiguration getTagsConfiguration() {
        return configurationService.loadTagsConfiguration();
    }

    /**
     * Saves the given tag configuration.
     *
     * @param tagsConfiguration The configuration to save.
     */
    @PostMapping(value = "/tags")
    public void saveTagsConfiguration(@RequestBody TagsConfiguration tagsConfiguration) {
        configurationService.saveTagsConfiguration(tagsConfiguration);
    }

    /**
     * Exports the current tags configuration as JSON file.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/tags/export")
    public ResponseEntity<StreamingResponseBody> exportTagsConfiguration(HttpServletResponse response) {
        String tagsConfigurationJson = configurationService.exportTagsConfiguration();

        StreamingResponseBody streamResponseBody = out -> {
            response.getOutputStream().write(tagsConfigurationJson.getBytes());
            response.setContentLength(tagsConfigurationJson.getBytes().length);
            configurationService.cleanupTagsConfigurationExport();
        };

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(streamResponseBody);
    }

    /**
     * Returns the adapter/peripherals configuration.
     *
     * @return The current adapter configuration.
     */
    @GetMapping(value = "/adapter")
    public AdapterConfiguration getAdapterConfiguration() {
        return configurationService.loadAdapterConfiguration();
    }

    /**
     * Saves the given adapter configuration
     *
     * @param adapterConfiguration The configuration to save.
     */
    @PostMapping(value = "/adapter")
    public void saveAdapterConfiguration(@RequestBody AdapterConfiguration adapterConfiguration) {
        configurationService.saveAdapterConfiguration(adapterConfiguration);
    }

    /**
     * Returns the exchange configuration.
     *
     * @return The current exchange configuration.
     */
    @GetMapping(value = "/exchange")
    public ExchangeConfiguration getExchangeConfiguration() {
        return configurationService.loadExchangeConfiguration();
    }

    /**
     * Saves the given exchange configuration.
     *
     * @param exchangeConfiguration The configuration to save.
     */
    @PostMapping(value = "/exchange")
    public void saveExchangeConfiguration(@RequestBody ExchangeConfiguration exchangeConfiguration) {
        configurationService.saveExchangeConfiguration(exchangeConfiguration);
    }

}
