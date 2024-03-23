package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ConfigurationEntityRepository;
import com.arassec.artivact.backend.persistence.model.ConfigurationEntity;
import com.arassec.artivact.backend.service.aop.GenerateIds;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.creator.adapter.AdapterImplementation;
import com.arassec.artivact.backend.service.exception.ArtivactException;
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
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Service for configuration management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService extends BaseService {

    /**
     * Repository to configuration entities.
     */
    private final ConfigurationEntityRepository configurationEntityRepository;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Spring's environment.
     */
    private final Environment environment;

    /**
     * The application's object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Loads the current property configuration.
     *
     * @return The properties currently configured.
     */
    @RestrictResult
    public PropertiesConfiguration loadPropertiesConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.PROPERTIES.name());
        if (StringUtils.hasText(entity.getContentJson())) {
            return fromJson(entity.getContentJson(), PropertiesConfiguration.class);
        }
        return new PropertiesConfiguration();
    }

    /**
     * Saves a property configuration.
     *
     * @param propertiesConfiguration The configuration to save.
     */
    @GenerateIds
    public void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        saveEntity(ConfigurationType.PROPERTIES, toJson(propertiesConfiguration));
    }

    /**
     * Loads the translated properties within their categories.
     *
     * @return The current properties.
     */
    @TranslateResult
    @RestrictResult
    public List<PropertyCategory> loadTranslatedProperties() {
        PropertiesConfiguration propertiesConfiguration = loadPropertiesConfiguration();
        return propertiesConfiguration.getCategories();
    }

    /**
     * Loads the current license configuration.
     *
     * @return The current license configuration.
     */
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

    /**
     * Saves a license configuration.
     *
     * @param licenseConfiguration The configuration to save.
     */
    public void saveLicenseConfiguration(LicenseConfiguration licenseConfiguration) {
        saveEntity(ConfigurationType.LICENSE, toJson(licenseConfiguration));
    }

    /**
     * Returns whether the application is run in desktop-mode or server-mode.
     *
     * @return {@code true} if the application is run in desktop-mode, {@code false} otherwise.
     */
    public boolean isDesktopMode() {
        return environment.matchesProfiles("desktop");
    }

    /**
     * Loads the current appearance configuration of the application.
     *
     * @return The current appearance configuration.
     */
    public AppearanceConfiguration loadAppearanceConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.APPEARANCE.name());
        if (StringUtils.hasText(entity.getContentJson())) {
            return fromJson(entity.getContentJson(), AppearanceConfiguration.class);
        } else {
            AppearanceConfiguration result = new AppearanceConfiguration();

            result.setApplicationTitle("Artivact");
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
                throw new ArtivactException("Could not read 16x16 pixel favicon!", e);
            }

            classPathResource = new ClassPathResource("icons/artivact-logo-32.ico", this.getClass().getClassLoader());
            try (InputStream is = classPathResource.getInputStream()) {
                result.setEncodedFaviconLarge(Base64.getEncoder().encodeToString(is.readAllBytes()));
            } catch (IOException e) {
                throw new ArtivactException("Could not read 32x32 pixel favicon!", e);
            }

            return result;
        }
    }

    /**
     * Saves an appearance configuration.
     *
     * @param appearanceConfiguration The configuration to save.
     */
    public void saveAppearanceConfiguration(AppearanceConfiguration appearanceConfiguration) {
        saveEntity(ConfigurationType.APPEARANCE, toJson(appearanceConfiguration));
    }

    /**
     * Loads the current tag configuration.
     *
     * @return The current tag configuration.
     */
    @RestrictResult
    @TranslateResult
    public TagsConfiguration loadTagsConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.TAGS.name());
        return fromJson(entity.getContentJson(), TagsConfiguration.class);
    }

    /**
     * Saves a tag configuration.
     *
     * @param tagsConfiguration The configuration to save.
     */
    @GenerateIds
    public void saveTagsConfiguration(TagsConfiguration tagsConfiguration) {
        saveEntity(ConfigurationType.TAGS, toJson(tagsConfiguration));
    }

    /**
     * Loads the current adapter configuration.
     *
     * @return The current adapter configuration.
     */
    public AdapterConfiguration loadAdapterConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.ADAPTER.name());
        AdapterConfiguration adapterConfiguration = fromJson(entity.getContentJson(), AdapterConfiguration.class);

        boolean windowsOs = System.getProperty("os.name").toLowerCase().contains("windows");

        // Initialize default values on first creation:
        if (adapterConfiguration.getBackgroundRemovalAdapterImplementation() == null) {
            adapterConfiguration.setBackgroundRemovalAdapterImplementation(AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER);
            adapterConfiguration.setCameraAdapterImplementation(AdapterImplementation.FALLBACK_CAMERA_ADAPTER);
            adapterConfiguration.setTurntableAdapterImplementation(AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER);
            adapterConfiguration.setModelCreatorImplementation(AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER);
            adapterConfiguration.setModelEditorImplementation(AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER);

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER, "1000");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER, "50");

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER, "");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER, "http://localhost:5000/api/remove");

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_CAMERA_ADAPTER, "");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER, "C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER, "http://localhost:5513/");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER, "/usr/bin/gphoto2");

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER, "");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER, "");

            if (windowsOs) {
                adapterConfiguration.getConfigValues().put(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER, "C:/Users/<USER>/Tools/Meshroom/meshroom_batch.exe");
                adapterConfiguration.getConfigValues().put(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER, "C:/Program Files/Agisoft/Metashape/metashape.exe");

                adapterConfiguration.getConfigValues().put(AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER, "C:/Users/<USER>/Tools/Blender/blender.exe");
            } else {
                adapterConfiguration.getConfigValues().put(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER, "~/tools/meshroom/meshroom_batch");
                adapterConfiguration.getConfigValues().put(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER, "~/tools/metashape/metashape.sh");

                adapterConfiguration.getConfigValues().put(AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER, "~/tools/blender/blender");
            }
        }

        // Initialize available options for the current platform:
        adapterConfiguration.getAvailableTurntableAdapterImplementations().add(AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER);
        adapterConfiguration.getAvailableTurntableAdapterImplementations().add(AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER);

        adapterConfiguration.getAvailableBackgroundRemovalAdapterImplementations().add(AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER);
        adapterConfiguration.getAvailableBackgroundRemovalAdapterImplementations().add(AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER);

        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER);
        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER);
        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER);

        adapterConfiguration.getAvailableModelEditorAdapterImplementations().add(AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER);
        adapterConfiguration.getAvailableModelEditorAdapterImplementations().add(AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER);

        adapterConfiguration.getAvailableCameraAdapterImplementations().add(AdapterImplementation.FALLBACK_CAMERA_ADAPTER);

        if (windowsOs) {
            adapterConfiguration.getAvailableCameraAdapterImplementations().add(AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER);
            adapterConfiguration.getAvailableCameraAdapterImplementations().add(AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER);
        } else {
            adapterConfiguration.getAvailableCameraAdapterImplementations().add(AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER);
        }

        return adapterConfiguration;
    }

    /**
     * Saves an adapter configuration.
     *
     * @param adapterConfiguration The configuration to save.
     */
    public void saveAdapterConfiguration(AdapterConfiguration adapterConfiguration) {
        // Available options are computed on load and must not be saved:
        adapterConfiguration.setAvailableBackgroundRemovalAdapterImplementations(List.of());
        adapterConfiguration.setAvailableCameraAdapterImplementations(List.of());
        adapterConfiguration.setAvailableTurntableAdapterImplementations(List.of());
        adapterConfiguration.setAvailableModelCreatorAdapterImplementations(List.of());
        adapterConfiguration.setAvailableModelEditorAdapterImplementations(List.of());
        saveEntity(ConfigurationType.ADAPTER, toJson(adapterConfiguration));
    }

    /**
     * Loads the current exchange configuration.
     *
     * @return The current exchange configuration.
     */
    public ExchangeConfiguration loadExchangeConfiguration() {
        ConfigurationEntity entity = loadOrCreateEntity(ConfigurationType.EXCHANGE.name());
        return fromJson(entity.getContentJson(), ExchangeConfiguration.class);
    }

    /**
     * Saves an exchange configuration.
     *
     * @param exchangeConfiguration The configuration to save.
     */
    public void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
        saveEntity(ConfigurationType.EXCHANGE, toJson(exchangeConfiguration));
    }

    /**
     * Loads translated menus.
     *
     * @return The currently configured, translated application menus.
     */
    @RestrictResult
    @TranslateResult
    public List<Menu> loadTranslatedMenus() {
        return fromJson(
                loadOrCreateEntity(ConfigurationType.MENU.name()).getContentJson(), MenuConfiguration.class).getMenus();
    }

    /**
     * Saves the given menus.
     *
     * @param menus The menus to save.
     * @return The updated, translated menus after saving.
     */
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

    /**
     * Saves a single menu.
     *
     * @param menu The menu to save.
     * @return All application menus, translated, and including the new menu.
     */
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

    /**
     * Deletes a menu.
     *
     * @param menuId The ID of the menu to delete.
     * @return All remaining application menus, translated.
     */
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

    /**
     * Adds an empty page to the given menu.
     *
     * @param menuId The ID of the menu to add a new page to.
     * @return All application menus, translated.
     */
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

    /**
     * Loads or creates a new configuration entity.
     *
     * @param id The ID of the configuration entity to load/create.
     * @return The entity.
     */
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

    /**
     * Saves a configuration entity.
     *
     * @param configurationType The type of configuration.
     * @param contentJson       The content to save.
     */
    private void saveEntity(ConfigurationType configurationType, String contentJson) {
        ConfigurationEntity entity = loadOrCreateEntity(configurationType.name());
        entity.setContentJson(contentJson);
        configurationEntityRepository.save(entity);
    }

}
