package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.UserData;
import com.arassec.artivact.backend.service.ConfigurationService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.Roles;
import com.arassec.artivact.backend.service.model.appearance.ColorTheme;
import com.arassec.artivact.backend.service.model.configuration.*;
import com.arassec.artivact.backend.service.model.menu.Menu;
import com.arassec.artivact.backend.service.model.property.PropertyCategory;
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
     * Returns the configured locales supported by the application. These are the locales maintained by the users for
     * every text in pages and items.
     *
     * @return The available locales as Strings.
     */
    @GetMapping(value = "/public/locale")
    public List<String> getAvailableLocales() {
        String availableLocales = configurationService.loadAppearanceConfiguration()
                .getAvailableLocales();
        if (StringUtils.hasText(availableLocales)) {
            return Arrays.stream(availableLocales.split(",")).map(String::trim).toList();
        } else {
            return List.of();
        }
    }

    /**
     * Returns the locale used by the application, which is maintained by the application. This determines the language
     * used in the application in desktop mode.
     *
     * @return The locale of the application.
     */
    @GetMapping(value = "/public/application-locale")
    public String getApplicationLocale() {
        return configurationService.loadAppearanceConfiguration().getApplicationLocale();
    }

    /**
     * Returns the available roles.
     *
     * @return The roles.
     */
    @GetMapping(value = "/public/role")
    public List<String> getAvailableRoles() {
        return List.of(Roles.ROLE_ADMIN, Roles.ROLE_USER);
    }

    /**
     * Returns the application's configured title.
     *
     * @return The title.
     */
    @GetMapping(value = "/public/title")
    public String getTitle() {
        return configurationService.loadAppearanceConfiguration().getApplicationTitle();
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
        return configurationService.loadTranslatedProperties();
    }

    /**
     * Returns the application's menu as configured by the user.
     *
     * @return The menu.
     */
    @GetMapping(value = "/public/menu")
    public List<Menu> getPublicMenus() {
        return configurationService.loadTranslatedMenus();
    }

    /**
     * Returns the current tag configuration.
     *
     * @return The tag configuration.
     */
    @GetMapping(value = "/public/tag")
    public TagsConfiguration getPublicTagsConfiguration() {
        return configurationService.loadTagsConfiguration();
    }

    /**
     * Returns the current license configuration.
     *
     * @return The license configuration.
     */
    @GetMapping(value = "/public/license")
    public LicenseConfiguration getPublicLicenseConfiguration() {
        return configurationService.loadLicenseConfiguration();
    }

    /**
     * Returns the current color theme configuration.
     *
     * @return The color theme configuration.
     */
    @GetMapping(value = "/public/colortheme")
    public ColorTheme getColorTheme() {
        return configurationService.loadAppearanceConfiguration().getColorTheme();
    }

    /**
     * Returns whether the application runs in desktop-mode ({@code true}) or not ({@code false}).
     *
     * @return {@code true} if the application runs in desktop-mode, {@code false} otherwise.
     */
    @GetMapping(value = "/public/desktop-mode")
    public boolean getDesktopMode() {
        return configurationService.isDesktopMode();
    }

    /**
     * Returns the application's favicon in the requested size.
     *
     * @param size The size in pixels.
     * @return The favicon as byte array.
     */
    @GetMapping(value = "/public/favicon/{size}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> getFavicon(@PathVariable Integer size) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadAppearanceConfiguration();

        String base64EncodedFavicon;
        if (size == 32) {
            base64EncodedFavicon = appearanceConfiguration.getEncodedFaviconLarge();
        } else {
            base64EncodedFavicon = appearanceConfiguration.getEncodedFaviconSmall();
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("image/ico"));

        return new HttpEntity<>(Base64.getDecoder().decode(base64EncodedFavicon), headers);
    }

    /**
     * Saves a favicon of 16x16 pixels size.
     *
     * @param file The new favicon to save.
     * @return HTTP-Status.
     */
    @PostMapping("/favicon/small")
    public ResponseEntity<Void> uploadSmallFavicon(@RequestPart(value = "file") final MultipartFile file) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadAppearanceConfiguration();
        try {
            appearanceConfiguration.setEncodedFaviconSmall(Base64.getEncoder().encodeToString(file.getBytes()));
            configurationService.saveAppearanceConfiguration(appearanceConfiguration);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ArtivactException("Could not Base64 encode small favicon!", e);
        }
    }

    /**
     * Saves a favicon of 32x32 pixels size.
     *
     * @param file The new favicon to save.
     * @return HTTP-Status.
     */
    @PostMapping("/favicon/large")
    public ResponseEntity<Void> uploadLargeFavicon(@RequestPart(value = "file") final MultipartFile file) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadAppearanceConfiguration();
        try {
            appearanceConfiguration.setEncodedFaviconLarge(Base64.getEncoder().encodeToString(file.getBytes()));
            configurationService.saveAppearanceConfiguration(appearanceConfiguration);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new ArtivactException("Could not Base64 encode small favicon!", e);
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
     * Returns the license configuration.
     *
     * @return The current license configuration.
     */
    @GetMapping(value = "/license")
    public LicenseConfiguration getLicenseConfiguration() {
        return configurationService.loadLicenseConfiguration();
    }

    /**
     * Saves the given license configuration.
     *
     * @param licenseConfiguration The license configuration to save.
     */
    @PostMapping(value = "/license")
    public void saveLicenseConfiguration(@RequestBody LicenseConfiguration licenseConfiguration) {
        configurationService.saveLicenseConfiguration(licenseConfiguration);
    }

    /**
     * Returns the appearance configuration.
     *
     * @return The current appearance configuration.
     */
    @GetMapping(value = "/appearance")
    public AppearanceConfiguration getAppearanceConfiguration() {
        return configurationService.loadAppearanceConfiguration();
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
