package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.service.ArtivactService;
import com.arassec.artivact.vault.backend.service.ConfigurationService;
import com.arassec.artivact.vault.backend.service.model.configuration.LicenseConfiguration;
import com.arassec.artivact.vault.backend.service.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.vault.backend.service.model.configuration.TagsConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/administration")
public class AdministrationController extends BaseController {

    private final ArtivactService artivactService;

    private final ConfigurationService configurationService;

    @PostMapping(value = "/scan")
    public void scanArtivactDir() {
        artivactService.scanDataDir();
    }

    @GetMapping(value = "/property")
    public PropertiesConfiguration getProperties(Authentication authentication) {
        return configurationService.loadPropertiesConfiguration(getRoles(authentication));
    }

    @PostMapping(value = "/property")
    public void saveProperties(@RequestBody PropertiesConfiguration propertiesConfiguration) {
        configurationService.savePropertiesConfiguration(propertiesConfiguration);
    }

    @GetMapping(value = "/license")
    public LicenseConfiguration getLicense(Authentication authentication) {
        return configurationService.loadLicenseConfiguration(getRoles(authentication));
    }

    @PostMapping(value = "/license")
    public void saveLicense(@RequestBody LicenseConfiguration licenseConfiguration) {
        configurationService.saveLicenseConfiguration(licenseConfiguration);
    }

    @GetMapping(value = "/tags")
    public TagsConfiguration getTags(Authentication authentication) {
        return configurationService.loadTagsConfiguration(getRoles(authentication));
    }

    @PostMapping(value = "/tags")
    public void saveTags(@RequestBody TagsConfiguration tagsConfiguration) {
        configurationService.saveTagsConfiguration(tagsConfiguration);
    }

}
