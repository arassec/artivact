package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.appearance.ColorTheme;
import com.arassec.artivact.core.model.appearance.License;
import com.arassec.artivact.core.model.configuration.*;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.repository.ConfigurationRepository;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.aspect.GenerateIds;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.exchange.ArtivactExporter;
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
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.arassec.artivact.domain.exchange.ExchangeProcessor.PROPERTIES_EXCHANGE_FILENAME_JSON;
import static com.arassec.artivact.domain.exchange.ExchangeProcessor.TAGS_EXCHANGE_FILENAME_JSON;

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
     * Spring's environment.
     */
    private final Environment environment;

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
     * Provider for project data.
     */
    private final ProjectDataProvider projectDataProvider;

    /**
     * Exporter for Artivact objects.
     */
    private final ArtivactExporter artivactExporter;

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
     * Exports the current property configuration and returns the export result as String.
     *
     * @return The exported property configuration.
     */
    public String exportPropertiesConfiguration() {
        return fileRepository.read(artivactExporter.exportPropertiesConfiguration(loadPropertiesConfiguration()));
    }

    /**
     * Removes a previously exported properties configuration file.
     */
    public void cleanupPropertiesConfigurationExport() {
        fileRepository.delete(projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(PROPERTIES_EXCHANGE_FILENAME_JSON));
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
     * Exports the current tags configuration and returns the result as String.
     *
     * @return The exported tags configuration.
     */
    public String exportTagsConfiguration() {
        return fileRepository.read(artivactExporter.exportTagsConfiguration(loadTagsConfiguration()));
    }

    /**
     * Removes a previously exported tags configuration file.
     */
    public void cleanupTagsConfigurationExport() {
        fileRepository.delete(projectDataProvider.getProjectRoot()
                .resolve(ProjectDataProvider.EXPORT_DIR)
                .resolve(TAGS_EXCHANGE_FILENAME_JSON));
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
