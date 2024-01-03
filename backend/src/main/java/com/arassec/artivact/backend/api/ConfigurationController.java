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

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;

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

    @GetMapping(value = "/public/role")
    public List<String> getAvailableRoles() {
        return List.of(Roles.ROLE_ADMIN, Roles.ROLE_USER);
    }

    @GetMapping(value = "/public/title")
    public String getTitle() {
        return configurationService.loadAppearanceConfiguration().getApplicationTitle();
    }

    @GetMapping(value = "/public/user")
    public UserData getUserDetails(Authentication authentication) {
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

    @GetMapping(value = "/public/property")
    public List<PropertyCategory> getPropertyCategories() {
        return configurationService.loadTranslatedProperties();
    }

    @GetMapping(value = "/public/menu")
    public List<Menu> getMenus() {
        return configurationService.loadTranslatedMenus();
    }

    @GetMapping(value = "/public/tag")
    public TagsConfiguration getTagsConfiguration() {
        return configurationService.loadTagsConfiguration();
    }

    @GetMapping(value = "/public/license")
    public LicenseConfiguration getTranslatedLicenseConfiguration() {
        return configurationService.loadLicenseConfiguration();
    }

    @GetMapping(value = "/public/colortheme")
    public ColorTheme getColorTheme() {
        return configurationService.loadAppearanceConfiguration().getColorTheme();
    }

    @GetMapping(value = "/public/desktop-mode")
    public boolean getDesktopMode() {
        return configurationService.isDesktopMode();
    }

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

    @PostMapping("/favicon/small")
    public ResponseEntity<String> uploadSmallFavicon(@RequestPart(value = "file") final MultipartFile file) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadAppearanceConfiguration();
        try {
            appearanceConfiguration.setEncodedFaviconSmall(Base64.getEncoder().encodeToString(file.getBytes()));
            configurationService.saveAppearanceConfiguration(appearanceConfiguration);
            return ResponseEntity.ok("small favicon uploaded");
        } catch (IOException e) {
            throw new ArtivactException("Could not Base64 encode small favicon!", e);
        }
    }

    @PostMapping("/favicon/large")
    public ResponseEntity<String> uploadLargeFavicon(@RequestPart(value = "file") final MultipartFile file) {
        AppearanceConfiguration appearanceConfiguration = configurationService.loadAppearanceConfiguration();
        try {
            appearanceConfiguration.setEncodedFaviconLarge(Base64.getEncoder().encodeToString(file.getBytes()));
            configurationService.saveAppearanceConfiguration(appearanceConfiguration);
            return ResponseEntity.ok("small favicon uploaded");
        } catch (IOException e) {
            throw new ArtivactException("Could not Base64 encode small favicon!", e);
        }
    }

    @GetMapping(value = "/property")
    public PropertiesConfiguration getProperties() {
        return configurationService.loadPropertiesConfiguration();
    }

    @PostMapping(value = "/property")
    public void saveProperties(@RequestBody PropertiesConfiguration propertiesConfiguration) {
        configurationService.savePropertiesConfiguration(propertiesConfiguration);
    }

    @GetMapping(value = "/license")
    public LicenseConfiguration getLicense() {
        return configurationService.loadLicenseConfiguration();
    }

    @PostMapping(value = "/license")
    public void saveLicense(@RequestBody LicenseConfiguration licenseConfiguration) {
        configurationService.saveLicenseConfiguration(licenseConfiguration);
    }

    @GetMapping(value = "/appearance")
    public AppearanceConfiguration getAppearanceConfig() {
        return configurationService.loadAppearanceConfiguration();
    }

    @PostMapping(value = "/appearance")
    public void saveAppearanceConfig(@RequestBody AppearanceConfiguration appearanceConfiguration) {
        configurationService.saveAppearanceConfiguration(appearanceConfiguration);
    }

    @GetMapping(value = "/tags")
    public TagsConfiguration getTags() {
        return configurationService.loadTagsConfiguration();
    }

    @PostMapping(value = "/tags")
    public void saveTags(@RequestBody TagsConfiguration tagsConfiguration) {
        configurationService.saveTagsConfiguration(tagsConfiguration);
    }

    @GetMapping(value = "/adapter")
    public AdapterConfiguration getAdapterConfig() {
        return configurationService.loadAdapterConfiguration();
    }

    @PostMapping(value = "/adapter")
    public void saveAdapterConfiguration(@RequestBody AdapterConfiguration adapterConfiguration) {
        configurationService.saveAdapterConfiguration(adapterConfiguration);
    }

    @PostMapping("/menu")
    public ResponseEntity<List<Menu>> saveMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(configurationService.saveMenu(menu));
    }

    @PostMapping("/menu/all")
    public ResponseEntity<List<Menu>> saveAllMenus(@RequestBody List<Menu> menus) {
        return ResponseEntity.ok(configurationService.saveMenus(menus));
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<List<Menu>> deleteMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(configurationService.deleteMenu(menuId));
    }

    @PostMapping("/menu/{menuId}/page")
    public ResponseEntity<List<Menu>> addPage(@PathVariable String menuId) {
        return ResponseEntity.ok(configurationService.addPageToMenu(menuId));
    }

}
