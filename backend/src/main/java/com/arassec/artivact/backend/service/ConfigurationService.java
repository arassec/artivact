package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ConfigurationEntityRepository;
import com.arassec.artivact.backend.persistence.model.ConfigurationEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.exception.VaultException;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.appearance.ColorTheme;
import com.arassec.artivact.backend.service.model.configuration.*;
import com.arassec.artivact.backend.service.model.menu.Menu;
import com.arassec.artivact.backend.service.model.page.Page;
import com.arassec.artivact.backend.service.model.property.PropertyCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService extends BaseService {

    private final ConfigurationEntityRepository configurationEntityRepository;

    private final PageService pageService;

    @Getter
    private final ObjectMapper objectMapper;

    @RestrictResult
    public PropertiesConfiguration loadPropertiesConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.PROPERTIES.name());
        if (StringUtils.hasText(entity.getContentJson())) {
            return fromJson(entity.getContentJson(), PropertiesConfiguration.class);
        }
        return new PropertiesConfiguration();
    }

    @GenerateIds
    public void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        saveEntity(ConfigurationType.PROPERTIES, toJson(propertiesConfiguration));
    }

    @TranslateResult
    @RestrictResult
    public List<PropertyCategory> loadTranslatedProperties() {
        PropertiesConfiguration propertiesConfiguration = loadPropertiesConfiguration();
        return propertiesConfiguration.getCategories();
    }

    @TranslateResult
    public LicenseConfiguration loadLicenseConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.LICENSE.name());
        if (StringUtils.hasText(entity.getContentJson())) {
            return fromJson(entity.getContentJson(), LicenseConfiguration.class);
        } else {
            LicenseConfiguration result = new LicenseConfiguration();
            result.setPrefix(new TranslatableString());
            result.setLicenseLabel(new TranslatableString());
            result.setSuffix(new TranslatableString());
            return result;
        }
    }

    public void saveLicenseConfiguration(LicenseConfiguration licenseConfiguration) {
        saveEntity(ConfigurationType.LICENSE, toJson(licenseConfiguration));
    }

    public AppearanceConfiguration loadAppearanceConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.APPEARANCE.name());
        if (StringUtils.hasText(entity.getContentJson())) {
            return fromJson(entity.getContentJson(), AppearanceConfiguration.class);
        } else {
            AppearanceConfiguration result = new AppearanceConfiguration();

            result.setApplicationTitle("Artivact-Vault");
            result.setAvailableLocales("");

            ColorTheme colorTheme = new ColorTheme();
            colorTheme.setPrimary("#6e7e85");
            colorTheme.setSecondary("#bbbac6");
            colorTheme.setAccent("#F5F5F5");
            colorTheme.setDark("#364958");
            colorTheme.setPositive("#87a330");
            colorTheme.setNegative("#a4031f");
            colorTheme.setInfo("#e2e2e2");
            colorTheme.setWarning("#e6c229");
            result.setColorTheme(colorTheme);

            ClassPathResource classPathResource = new ClassPathResource("icons/artivact-logo-16.ico", this.getClass().getClassLoader());
            try (InputStream is = classPathResource.getInputStream()) {
                result.setEncodedFaviconSmall(Base64.getEncoder().encodeToString(is.readAllBytes()));
            } catch (IOException e) {
                throw new VaultException("Could not read 16x16 pixel favicon!", e);
            }

            classPathResource = new ClassPathResource("icons/artivact-logo-32.ico", this.getClass().getClassLoader());
            try (InputStream is = classPathResource.getInputStream()) {
                result.setEncodedFaviconLarge(Base64.getEncoder().encodeToString(is.readAllBytes()));
            } catch (IOException e) {
                throw new VaultException("Could not read 32x32 pixel favicon!", e);
            }

            return result;
        }
    }

    public void saveAppearanceConfiguration(AppearanceConfiguration appearanceConfiguration) {
        saveEntity(ConfigurationType.APPEARANCE, toJson(appearanceConfiguration));
    }

    @RestrictResult
    @TranslateResult
    public TagsConfiguration loadTagsConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.TAGS.name());
        return fromJson(entity.getContentJson(), TagsConfiguration.class);
    }

    @GenerateIds
    public void saveTagsConfiguration(TagsConfiguration tagsConfiguration) {
        saveEntity(ConfigurationType.TAGS, toJson(tagsConfiguration));
    }

    @RestrictResult
    @TranslateResult
    public List<Menu> loadTranslatedMenus() {
        return fromJson(
                loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class).getMenus();
    }

    @GenerateIds
    @RestrictResult
    @TranslateResult
    public List<Menu> saveMenus(List<Menu> menus) {
        MenuConfiguration menuConfiguration = fromJson(
                loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class);
        menuConfiguration.setMenus(menus);
        saveEntity(ConfigurationType.MENU, toJson(menuConfiguration));

        menuConfiguration.getMenus().forEach(menu -> {
            if (StringUtils.hasText(menu.getTargetPageId())) {
                pageService.updatePageRestrictions(menu.getTargetPageId(), menu.getRestrictions());
            }
            menu.getMenuEntries().forEach(menuEntry ->
                    pageService.updatePageRestrictions(menuEntry.getTargetPageId(), menuEntry.getRestrictions()));
        });

        return loadTranslatedMenus();
    }

    @GenerateIds
    @RestrictResult
    @TranslateResult
    public List<Menu> saveMenu(Menu menu) {
        if (menu == null) {
            return loadTranslatedMenus();
        }

        MenuConfiguration menuConfiguration = fromJson(loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class);

        Optional<Menu> existingMenuOptional = menuConfiguration.getMenus().stream()
                .filter(existingMenu -> existingMenu.getId().equals(menu.getId()))
                .findFirst();

        if (existingMenuOptional.isPresent()) {
            Menu existingMenu = existingMenuOptional.get();
            existingMenu.setValue(menu.getValue());
            existingMenu.setTranslations(menu.getTranslations());
            existingMenu.setRestrictions(menu.getRestrictions());
            existingMenu.setTargetPageId(menu.getTargetPageId());
            existingMenu.setMenuEntries(menu.getMenuEntries());
        } else {
            menuConfiguration.getMenus().add(menu);
        }

        if (StringUtils.hasText(menu.getTargetPageId())) {
            pageService.updatePageRestrictions(menu.getTargetPageId(), menu.getRestrictions());
        }

        // We add pages for sub-menu entries automatically:
        menuConfiguration.getMenus().forEach(existingMenu ->
                existingMenu.getMenuEntries().forEach(existingMenuEntry -> {
                    if (!StringUtils.hasText(existingMenuEntry.getTargetPageId())) {
                        Page page = pageService.createPage(existingMenuEntry.getRestrictions());
                        existingMenuEntry.setTargetPageId(page.getId());
                    }
                }));

        saveEntity(ConfigurationType.MENU, toJson(menuConfiguration));

        return loadTranslatedMenus();
    }

    @RestrictResult
    @TranslateResult
    public List<Menu> deleteMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to delete menu!");
            return loadTranslatedMenus();
        }

        MenuConfiguration menuConfiguration = fromJson(loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class);

        List<String> pagesToDelete = new LinkedList<>();

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                // Main menu to delete:
                pagesToDelete.addAll(menu.getMenuEntries().stream()
                        .map(Menu::getTargetPageId)
                        .toList());
                pagesToDelete.add(menu.getTargetPageId());
            } else {
                // Search menu entries for possible delete candidate:
                pagesToDelete.addAll(menu.getMenuEntries().stream()
                        .filter(menuEntry -> menuEntry.getId().equals(menuId))
                        .map(Menu::getTargetPageId)
                        .toList());
            }
        });

        pagesToDelete.stream()
                .filter(Objects::nonNull)
                .forEach(pageService::deletePage);

        // Delete sub-menus:
        menuConfiguration.getMenus()
                .forEach(menu -> menu.setMenuEntries(menu.getMenuEntries().stream()
                        .filter(menuEntry -> !menuEntry.getId().equals(menuId))
                        .toList()));

        menuConfiguration.setMenus(menuConfiguration.getMenus().stream()
                .filter(existingMenu -> !existingMenu.getId().equals(menuId))
                .toList());

        saveEntity(ConfigurationType.MENU, toJson(menuConfiguration));

        return loadTranslatedMenus();
    }

    @RestrictResult
    @TranslateResult
    public List<Menu> addPageToMenu(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            log.warn("No menuId given to add page to!");
            return loadTranslatedMenus();
        }

        MenuConfiguration menuConfiguration = fromJson(
                loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class);

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                Page page = pageService.createPage(menu.getRestrictions());
                menu.setTargetPageId(page.getId());
            }
        });

        saveEntity(ConfigurationType.MENU, toJson(menuConfiguration));

        return loadTranslatedMenus();
    }

    private ConfigurationEntity loadOrCreateEntity(String id) {
        Optional<ConfigurationEntity> entityOptional = configurationEntityRepository.findById(id);

        ConfigurationEntity entity;
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
        } else {
            entity = new ConfigurationEntity();
            entity.setId(id);
        }

        return entity;
    }

    private void saveEntity(ConfigurationType configurationType, String contentJson) {
        ConfigurationEntity entity = loadOrCreateEntity(configurationType.name());
        entity.setContentJson(contentJson);
        configurationEntityRepository.save(entity);
    }

}
