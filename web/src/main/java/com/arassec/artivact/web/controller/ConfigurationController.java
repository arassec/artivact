package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.configuration.*;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.domain.service.ConfigurationService;
import com.arassec.artivact.web.model.ApplicationSettings;
import com.arassec.artivact.web.model.Profiles;
import com.arassec.artivact.web.model.UserData;
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

import java.io.IOException;
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
public class ConfigurationController {

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
     * Returns the application's menu as configured by the user.
     *
     * @return The menu.
     */
    @GetMapping(value = "/public/menu")
    public List<Menu> getPublicMenus() {
        return configurationService.loadTranslatedRestrictedMenus();
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
     * @return The favicon as byte array.
     */
    @GetMapping(value = "/public/favicon", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> getFavicon() {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadTranslatedAppearanceConfiguration();

        String base64EncodedFavicon = appearanceConfiguration.getEncodedFavicon();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("image/ico"));

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

    /**
     * Saves a menu.
     *
     * @param menu The menu to save.
     * @return The list of all menus of the application.
     */
    @PostMapping("/menu")
    public ResponseEntity<List<Menu>> saveMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(configurationService.saveMenu(menu));
    }

    /**
     * Saves all menus.
     *
     * @param menus The menus to save.
     * @return The list of all menus of the application.
     */
    @PostMapping("/menu/all")
    public ResponseEntity<List<Menu>> saveAllMenus(@RequestBody List<Menu> menus) {
        return ResponseEntity.ok(configurationService.saveMenus(menus));
    }

    /**
     * Deletes a single menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<List<Menu>> deleteMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(configurationService.deleteMenu(menuId));
    }

    /**
     * Adds a page to a menu.
     *
     * @param menuId The menu's ID.
     * @return The list of all menus of the application.
     */
    @PostMapping("/menu/{menuId}/page")
    public ResponseEntity<List<Menu>> addPage(@PathVariable String menuId) {
        return ResponseEntity.ok(configurationService.addPageToMenu(menuId));
    }

}
