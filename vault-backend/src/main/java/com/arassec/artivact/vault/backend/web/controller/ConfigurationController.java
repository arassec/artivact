package com.arassec.artivact.vault.backend.web.controller;

import com.arassec.artivact.vault.backend.core.Roles;
import com.arassec.artivact.vault.backend.service.ConfigurationService;
import com.arassec.artivact.vault.backend.service.model.TranslatedMenu;
import com.arassec.artivact.vault.backend.service.model.TranslatedPropertyCategory;
import com.arassec.artivact.vault.backend.service.model.configuration.LicenseConfiguration;
import com.arassec.artivact.vault.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.vault.backend.web.model.TranslatedLicense;
import com.arassec.artivact.vault.backend.web.model.TranslatedTag;
import com.arassec.artivact.vault.backend.web.model.TranslatedTagsConfiguration;
import com.arassec.artivact.vault.backend.web.model.UserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController extends BaseController {

    private final ConfigurationService configurationService;

    @Value("${artivact.vault.available.locales}")
    private List<String> availableLocales;

    @Value("${artivact.vault.title}")
    private String title;

    @GetMapping(value = "/locale")
    public List<String> getAvailableLocales() {
        return availableLocales;
    }

    @GetMapping(value = "/role")
    public List<String> getAvailableRoles() {
        return List.of(Roles.ROLE_ADMIN, Roles.ROLE_USER);
    }

    @GetMapping(value = "/title")
    public String getTitle() {
        return title;
    }

    @GetMapping(value = "/user")
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

    @GetMapping(value = "/property/translated")
    public List<TranslatedPropertyCategory> getTranslatedProperties(Locale locale, Authentication authentication) {
        return configurationService.loadTranslatedPropertyCategories(locale, getRoles(authentication));
    }

    @GetMapping(value = "/menu")
    public List<TranslatedMenu> getTranslatedMenu(Locale locale, Authentication authentication) {
        return configurationService.loadTranslatedMenus(locale, getRoles(authentication));
    }

    @GetMapping(value = "/license")
    public TranslatedLicense getTranslatedLicenseConfiguration(Locale locale, Authentication authentication) {
        LicenseConfiguration licenseConfiguration = configurationService.loadLicenseConfiguration(getRoles(authentication));

        TranslatedLicense result = new TranslatedLicense();

        result.setPrefix(licenseConfiguration.getPrefix().getTranslatedValue(locale.toString()));
        result.setLicenseLabel(licenseConfiguration.getLicenseLabel().getTranslatedValue(locale.toString()));
        result.setSuffix(licenseConfiguration.getSuffix().getTranslatedValue(locale.toString()));

        result.setLicenseUrl(licenseConfiguration.getLicenseUrl());

        return result;
    }

    @GetMapping(value = "/tags/translated")
    public TranslatedTagsConfiguration getTranslatedTagsConfiguration(Locale locale, Authentication authentication) {
        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration(getRoles(authentication));

        TranslatedTagsConfiguration result = new TranslatedTagsConfiguration();

        result.setTags(tagsConfiguration.getTags().stream()
                .map(tag -> {
                    TranslatedTag translatedTag = new TranslatedTag();
                    translatedTag.setId(tag.getId());
                    translatedTag.setTranslatedValue(tag.getTranslatedValue(locale.toString()));
                    translatedTag.setUrl(tag.getUrl());
                    return translatedTag;
                })
                .toList()
        );

        return result;
    }

}
