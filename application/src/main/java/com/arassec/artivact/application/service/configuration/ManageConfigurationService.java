package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.infrastructure.aspect.GenerateIds;
import com.arassec.artivact.application.infrastructure.aspect.RestrictResult;
import com.arassec.artivact.application.infrastructure.aspect.TranslateResult;
import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.domain.model.configuration.*;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    public PeripheralsConfiguration loadPeripheralConfiguration() {
        Optional<PeripheralsConfiguration> configurationOptional =
                configurationRepository.findByType(ConfigurationType.PERIPHERALS, PeripheralsConfiguration.class);

        PeripheralsConfiguration peripheralsConfiguration = configurationOptional.orElseGet(PeripheralsConfiguration::new);
        peripheralsConfiguration.setTurntablePeripheralConfigs(peripheralsConfiguration.getTurntablePeripheralConfigs().stream()
                .filter(Objects::nonNull).toList());
        peripheralsConfiguration.setCameraPeripheralConfigs(peripheralsConfiguration.getCameraPeripheralConfigs().stream()
                .filter(Objects::nonNull).toList());
        peripheralsConfiguration.setImageBackgroundRemovalPeripheralConfigs(peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs().stream()
                .filter(Objects::nonNull).toList());
        peripheralsConfiguration.setModelCreatorPeripheralConfigs(peripheralsConfiguration.getModelCreatorPeripheralConfigs().stream()
                .filter(Objects::nonNull).toList());
        peripheralsConfiguration.setModelEditorPeripheralConfigs(peripheralsConfiguration.getModelEditorPeripheralConfigs().stream()
                .filter(Objects::nonNull).toList());

        // Initialize available options for the current platform:
        peripheralsConfiguration.getAvailableTurntablePeripheralImplementations().add(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);
        peripheralsConfiguration.getAvailableImageManipulatorPeripheralImplementations().add(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);
        peripheralsConfiguration.getAvailableCameraPeripheralImplementations().add(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);
        peripheralsConfiguration.getAvailableCameraPeripheralImplementations().add(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
        peripheralsConfiguration.getAvailableModelCreatorPeripheralImplementations().add(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
        peripheralsConfiguration.getAvailableModelEditorPeripheralImplementations().add(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);

        return peripheralsConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GenerateIds
    public void savePeripheralConfiguration(PeripheralsConfiguration peripheralConfiguration) {
        // Available options are computed when loading and must not be saved:
        peripheralConfiguration.setAvailableImageManipulatorPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableCameraPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableTurntablePeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableModelCreatorPeripheralImplementations(List.of());
        peripheralConfiguration.setAvailableModelEditorPeripheralImplementations(List.of());
        configurationRepository.saveConfiguration(ConfigurationType.PERIPHERALS, peripheralConfiguration);
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
