package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.appearance.ColorTheme;
import com.arassec.artivact.core.model.appearance.License;
import com.arassec.artivact.core.model.configuration.*;
import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.repository.ConfigurationRepository;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Service for configuration management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConfigurationService extends BaseFileService {

    /**
     * Repository to configurations.
     */
    private final ConfigurationRepository configurationRepository;

    /**
     * Service for page handling.
     */
    private final PageService pageService;

    /**
     * Spring's environment.
     */
    private final Environment environment;

    /**
     * The application's {@link ProjectDataProvider}.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * The application's {@link FileRepository}.
     */
    @Getter
    private final FileRepository fileRepository;

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
    public PropertiesConfiguration loadPropertiesConfiguration() {
        Optional<PropertiesConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.PROPERTIES, PropertiesConfiguration.class);
        return configurationOptional.orElseGet(PropertiesConfiguration::new);
    }

    /**
     * Saves a property configuration.
     *
     * @param propertiesConfiguration The configuration to save.
     */
    @GenerateIds
    public void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.PROPERTIES, propertiesConfiguration);
    }

    /**
     * Loads the translated properties within their categories.
     *
     * @return The current properties.
     */
    @TranslateResult
    @RestrictResult
    public List<PropertyCategory> loadTranslatedRestrictedProperties() {
        PropertiesConfiguration propertiesConfiguration = loadPropertiesConfiguration();
        return propertiesConfiguration.getCategories();
    }

    /**
     * Returns whether the application is run in desktop-mode.
     *
     * @return {@code true} if the application is run in desktop-mode, {@code false} otherwise.
     */
    public boolean isDesktopProfileEnabled() {
        return environment.matchesProfiles("desktop");
    }

    /**
     * Returns whether the application is run in E2E-mode.
     *
     * @return {@code true} if the application is run in E2E-mode, {@code false} otherwise.
     */
    public boolean isE2eProfileEnabled() {
        return environment.matchesProfiles("e2e");
    }

    /**
     * Loads the current appearance configuration of the application.
     *
     * @return The current appearance configuration.
     */
    @TranslateResult
    public AppearanceConfiguration loadTranslatedAppearanceConfiguration() {
        Optional<AppearanceConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class);

        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();

        if (configurationOptional.isPresent()) {
            appearanceConfiguration = configurationOptional.get();
            if (!StringUtils.hasText(appearanceConfiguration.getEncodedFavicon())) {
                setDefaultFavicon(appearanceConfiguration);
            }
            if (appearanceConfiguration.getLicense() == null) {
                appearanceConfiguration.setLicense(new License());
            }
        } else {
            appearanceConfiguration.setApplicationTitle("Artivact");
            appearanceConfiguration.setAvailableLocales("");
            appearanceConfiguration.setLicense(new License());

            ColorTheme colorTheme = new ColorTheme();
            colorTheme.setPrimary("#6e7e85");
            colorTheme.setSecondary("#bbbac6");
            colorTheme.setAccent("#F5F5F5");
            colorTheme.setDark("#364958");
            colorTheme.setPositive("#87a330");
            colorTheme.setNegative("#a4031f");
            colorTheme.setInfo("#e2e2e2");
            colorTheme.setWarning("#e6c229");
            appearanceConfiguration.setColorTheme(colorTheme);

            setDefaultFavicon(appearanceConfiguration);
        }

        return appearanceConfiguration;
    }

    /**
     * Saves an appearance configuration.
     *
     * @param appearanceConfiguration The configuration to save.
     */
    public void saveAppearanceConfiguration(AppearanceConfiguration appearanceConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.APPEARANCE, appearanceConfiguration);
    }

    /**
     * Loads the current tag configuration.
     *
     * @return The current tag configuration.
     */
    public TagsConfiguration loadTagsConfiguration() {
        Optional<TagsConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class);
        return configurationOptional.orElseGet(TagsConfiguration::new);
    }

    /**
     * Loads the current tag configuration, restricted and translated.
     *
     * @return The current tag configuration.
     */
    @RestrictResult
    @TranslateResult
    public TagsConfiguration loadTranslatedRestrictedTags() {
        return loadTagsConfiguration();
    }

    /**
     * Saves a tag configuration.
     *
     * @param tagsConfiguration The configuration to save.
     */
    @GenerateIds
    public void saveTagsConfiguration(TagsConfiguration tagsConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.TAGS, tagsConfiguration);
    }

    /**
     * Loads the current adapter configuration.
     *
     * @return The current adapter configuration.
     */
    public AdapterConfiguration loadAdapterConfiguration() {
        Optional<AdapterConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.ADAPTER, AdapterConfiguration.class);

        AdapterConfiguration adapterConfiguration = configurationOptional.orElseGet(AdapterConfiguration::new);

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
            adapterConfiguration.getConfigValues().put(AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER, "http://localhost:7000/api/remove");

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_CAMERA_ADAPTER, "");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.DIGI_CAM_CONTROL_CAMERA_ADAPTER, "C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER, "http://localhost:5513/");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.GPHOTO_TWO_CAMERA_ADAPTER, "/usr/bin/gphoto2");

            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER, "");
            adapterConfiguration.getConfigValues().put(AdapterImplementation.FALLBACK_MODEL_EDITOR_ADAPTER, "");

            if (windowsOs) {
                adapterConfiguration.getConfigValues().put(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER, "C:/Users/<USER>/Tools/Meshroom/Meshroom.exe");
                adapterConfiguration.getConfigValues().put(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER, "C:/Program Files/Agisoft/Metashape/metashape.exe");
                adapterConfiguration.getConfigValues().put(AdapterImplementation.REALITY_CAPTURE_MODEL_CREATOR_ADAPTER, "C:/Program Files/Capturing Reality/RealityCapture/RealityCapture.exe");

                adapterConfiguration.getConfigValues().put(AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER, "C:/Users/<USER>/Tools/Blender/blender.exe");
            } else {
                adapterConfiguration.getConfigValues().put(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER, "~/tools/meshroom/Meshroom");
                adapterConfiguration.getConfigValues().put(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER, "~/tools/metashape/metashape.sh");

                adapterConfiguration.getConfigValues().put(AdapterImplementation.BLENDER_MODEL_EDITOR_ADAPTER, "~/tools/blender/blender");
            }
        } else if (windowsOs && adapterConfiguration.getConfigValues().get(AdapterImplementation.REALITY_CAPTURE_MODEL_CREATOR_ADAPTER) == null) {
            adapterConfiguration.getConfigValues().put(AdapterImplementation.REALITY_CAPTURE_MODEL_CREATOR_ADAPTER, "C:/Program Files/Capturing Reality/RealityCapture/RealityCapture.exe");
        }

        // Initialize available options for the current platform:
        adapterConfiguration.getAvailableTurntableAdapterImplementations().add(AdapterImplementation.FALLBACK_TURNTABLE_ADAPTER);
        adapterConfiguration.getAvailableTurntableAdapterImplementations().add(AdapterImplementation.ARTIVACT_TURNTABLE_ADAPTER);

        adapterConfiguration.getAvailableBackgroundRemovalAdapterImplementations().add(AdapterImplementation.FALLBACK_BACKGROUND_REMOVAL_ADAPTER);
        adapterConfiguration.getAvailableBackgroundRemovalAdapterImplementations().add(AdapterImplementation.REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER);

        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.FALLBACK_MODEL_CREATOR_ADAPTER);
        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.MESHROOM_MODEL_CREATOR_ADAPTER);
        adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.METASHAPE_MODEL_CREATOR_ADAPTER);
        if (windowsOs) {
            adapterConfiguration.getAvailableModelCreatorAdapterImplementations().add(AdapterImplementation.REALITY_CAPTURE_MODEL_CREATOR_ADAPTER);
        }

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
        configurationRepository.saveConfiguration(ConfigurationType.ADAPTER, adapterConfiguration);
    }

    /**
     * Loads the current exchange configuration.
     *
     * @return The current exchange configuration.
     */
    public ExchangeConfiguration loadExchangeConfiguration() {
        Optional<ExchangeConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class);
        return configurationOptional.orElseGet(ExchangeConfiguration::new);
    }

    /**
     * Saves an exchange configuration.
     *
     * @param exchangeConfiguration The configuration to save.
     */
    public void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.EXCHANGE, exchangeConfiguration);
    }

    /**
     * Loads translated menus.
     *
     * @return The currently configured, translated application menus.
     */
    @RestrictResult
    @TranslateResult
    public List<Menu> loadTranslatedRestrictedMenus() {
        Optional<MenuConfiguration> configurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        MenuConfiguration menuConfiguration = configurationOptional.orElseGet(MenuConfiguration::new);

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getExportTitle() == null) {
                menu.setExportTitle(new TranslatableString());
            }
            if (menu.getExportDescription() == null) {
                menu.setExportDescription(new TranslatableString());
            }
            menu.getMenuEntries().forEach(menuEntry -> {
                if (menuEntry.getExportTitle() == null) {
                    menuEntry.setExportTitle(new TranslatableString());
                }
                if (menuEntry.getExportDescription() == null) {
                    menuEntry.setExportDescription(new TranslatableString());
                }
            });
        });

        return menuConfiguration.getMenus();
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
        Optional<MenuConfiguration> configurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        MenuConfiguration menuConfiguration = configurationOptional.orElseGet(MenuConfiguration::new);

        menuConfiguration.setMenus(menus);

        configurationRepository.saveConfiguration(ConfigurationType.MENU, menuConfiguration);

        menuConfiguration.getMenus().forEach(menu -> {
            if (StringUtils.hasText(menu.getTargetPageId())) {
                pageService.updatePageRestrictions(menu.getTargetPageId(), menu.getRestrictions());
            }
            menu.getMenuEntries().forEach(menuEntry ->
                    pageService.updatePageRestrictions(menuEntry.getTargetPageId(), menuEntry.getRestrictions()));
        });

        return loadTranslatedRestrictedMenus();
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
            return loadTranslatedRestrictedMenus();
        }

        if (!StringUtils.hasText(menu.getValue())) {
            throw new ArtivactException("Menu title required!");
        }

        menu.getMenuEntries().forEach(menuEntry -> {
            if (!StringUtils.hasText(menuEntry.getValue())) {
                throw new ArtivactException("Sub-Menu title required!");
            }
        });

        Optional<MenuConfiguration> configurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        MenuConfiguration menuConfiguration = configurationOptional.orElseGet(MenuConfiguration::new);

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
            existingMenu.setExportTitle(menu.getExportTitle());
            existingMenu.setExportDescription(menu.getExportDescription());
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

        configurationRepository.saveConfiguration(ConfigurationType.MENU, menuConfiguration);

        return loadTranslatedRestrictedMenus();
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
            return loadTranslatedRestrictedMenus();
        }

        Optional<MenuConfiguration> configurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        MenuConfiguration menuConfiguration = configurationOptional.orElseGet(MenuConfiguration::new);

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

        configurationRepository.saveConfiguration(ConfigurationType.MENU, menuConfiguration);

        return loadTranslatedRestrictedMenus();
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
            return loadTranslatedRestrictedMenus();
        }

        Optional<MenuConfiguration> configurationOptional = configurationRepository.findByType(ConfigurationType.MENU, MenuConfiguration.class);
        MenuConfiguration menuConfiguration = configurationOptional.orElseGet(MenuConfiguration::new);

        menuConfiguration.getMenus().forEach(menu -> {
            if (menu.getId().equals(menuId)) {
                Page page = pageService.createPage(menu.getRestrictions());
                menu.setTargetPageId(page.getId());
            }
        });

        configurationRepository.saveConfiguration(ConfigurationType.MENU, menuConfiguration);

        return loadTranslatedRestrictedMenus();
    }

    /**
     * Saves a menu's cover picture, e.g. for exports.
     *
     * @param menuId           The menu the cover picture is assigned to.
     * @param originalFilename The original filename of the cover picture.
     * @param inputStream      The input stream containing the picture.
     */
    public void saveMenuCoverPicture(String menuId, String originalFilename, InputStream inputStream) {
        String fileExtension = getExtension(originalFilename).orElseThrow();

        Path targetDir = fileRepository.getSubdirFilePath(projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.MENUS_DIR), menuId, null);
        fileRepository.createDirIfRequired(targetDir);

        Path coverPicture = targetDir.resolve("cover-picture." + fileExtension);

        getFileRepository().scaleImage(inputStream, coverPicture, fileExtension, ImageSize.PAGE_TITLE.getWidth());
    }

    /**
     * Sets Artivact's default favicon to the appearance configuration.
     *
     * @param appearanceConfiguration The configuration to update.
     */
    private void setDefaultFavicon(AppearanceConfiguration appearanceConfiguration) {
        ClassPathResource classPathResource = new ClassPathResource("icons/favicon-32x32.ico", this.getClass().getClassLoader());
        try (InputStream is = classPathResource.getInputStream()) {
            appearanceConfiguration.setEncodedFavicon(Base64.getEncoder().encodeToString(is.readAllBytes()));
        } catch (IOException e) {
            throw new ArtivactException("Could not read 32x32 pixel favicon!", e);
        }
    }

}
