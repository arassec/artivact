package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.*;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for configuration management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManageConfigurationService
        implements LoadPropertiesConfigurationUseCase,
        SavePropertiesConfigurationUseCase,
        LoadTagsConfigurationUseCase,
        SaveTagsConfigurationUseCase,
        LoadExchangeConfigurationUseCase,
        LoadAppearanceConfigurationUseCase,
        SaveAppearanceConfigurationUseCase,
        LoadPeripheralConfigurationUseCase,
        SavePeripheralConfigurationUseCase,
        SaveExchangeConfigurationUseCase,
        CheckRuntimeConfigurationUseCase {

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
     * {@inheritDoc}
     */
    @Override
    public PropertiesConfiguration loadPropertiesConfiguration() {
        Optional<PropertiesConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.PROPERTIES, PropertiesConfiguration.class);
        return configurationOptional.orElseGet(PropertiesConfiguration::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GenerateIds
    public void savePropertiesConfiguration(PropertiesConfiguration propertiesConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.PROPERTIES, propertiesConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TranslateResult
    @RestrictResult
    public List<PropertyCategory> loadTranslatedRestrictedProperties() {
        PropertiesConfiguration propertiesConfiguration = loadPropertiesConfiguration();
        return propertiesConfiguration.getCategories();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDesktopProfileEnabled() {
        return environment.matchesProfiles("desktop");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isE2eProfileEnabled() {
        return environment.matchesProfiles("e2e");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TranslateResult
    public AppearanceConfiguration loadTranslatedAppearanceConfiguration() {
        Optional<AppearanceConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class);
        return configurationOptional.orElseGet(AppearanceConfiguration::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAppearanceConfiguration(AppearanceConfiguration appearanceConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.APPEARANCE, appearanceConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagsConfiguration loadTagsConfiguration() {
        Optional<TagsConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class);
        return configurationOptional.orElseGet(TagsConfiguration::new);
    }

    /**
     * {@inheritDoc}
     */
    @RestrictResult
    @TranslateResult
    @Override
    public TagsConfiguration loadTranslatedRestrictedTagsConfiguration() {
        return loadTagsConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @GenerateIds
    @Override
    public void saveTagsConfiguration(TagsConfiguration tagsConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.TAGS, tagsConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralConfiguration loadPeripheralConfiguration() {
        Optional<PeripheralConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.PERIPHERAL, PeripheralConfiguration.class);

        PeripheralConfiguration peripheralConfiguration = configurationOptional.orElseGet(PeripheralConfiguration::new);

        // Initialize available options for the current platform:
        peripheralConfiguration.getAvailableTurntablePeripheralImplementations().add(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL);
        peripheralConfiguration.getAvailableImageManipulationPeripheralImplementations().add(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);
        peripheralConfiguration.getAvailableCameraPeripheralImplementations().add(PeripheralImplementation.DEFAULT_CAMERA_PERIPHERAL);

        boolean windowsOs = System.getProperty("os.name").toLowerCase().contains("windows");

        if (windowsOs) {
            peripheralConfiguration.getAvailableCameraPeripheralImplementations().add(PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL);
        } else {
            peripheralConfiguration.getAvailableCameraPeripheralImplementations().add(PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL);
        }

        peripheralConfiguration.getAvailableModelCreatorPeripheralImplementations().add(PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL);
        peripheralConfiguration.getAvailableModelCreatorPeripheralImplementations().add(PeripheralImplementation.MESHROOM_MODEL_CREATOR_PERIPHERAL);
        peripheralConfiguration.getAvailableModelCreatorPeripheralImplementations().add(PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL);
        if (windowsOs) {
            peripheralConfiguration.getAvailableModelCreatorPeripheralImplementations().add(PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL);
        }

        peripheralConfiguration.getAvailableModelEditorPeripheralImplementations().add(PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL);
        peripheralConfiguration.getAvailableModelEditorPeripheralImplementations().add(PeripheralImplementation.BLENDER_MODEL_EDITOR_PERIPHERAL);

        return peripheralConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void savePeripheralConfiguration(PeripheralConfiguration peripheralConfiguration) {
        // Available options are computed when loading and must not be saved:
        peripheralConfiguration.setAvailableImageManipulationPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableCameraPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableTurntablePeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableModelCreatorPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableModelEditorPeripheralImplementations(List.of());
        configurationRepository.saveConfiguration(ConfigurationType.PERIPHERAL, peripheralConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExchangeConfiguration loadExchangeConfiguration() {
        Optional<ExchangeConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class);
        return configurationOptional.orElseGet(ExchangeConfiguration::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
        configurationRepository.saveConfiguration(ConfigurationType.EXCHANGE, exchangeConfiguration);
    }

}
