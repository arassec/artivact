package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.domain.model.configuration.*;
import com.arassec.artivact.domain.model.property.PropertyCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageConfigurationServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private ManageConfigurationService service;

    private PropertiesConfiguration propertiesConfiguration;

    @BeforeEach
    void setUp() {
        propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setCategories(List.of(new PropertyCategory()));
    }

    @Test
    void testLoadPropertiesConfigurationFound() {
        when(configurationRepository.findByType(ConfigurationType.PROPERTIES, PropertiesConfiguration.class))
                .thenReturn(Optional.of(propertiesConfiguration));

        var result = service.loadPropertiesConfiguration();

        assertThat(result).isEqualTo(propertiesConfiguration);
    }

    @Test
    void testLoadPropertiesConfigurationNotFound() {
        when(configurationRepository.findByType(ConfigurationType.PROPERTIES, PropertiesConfiguration.class))
                .thenReturn(Optional.empty());

        var result = service.loadPropertiesConfiguration();

        assertThat(result).isNotNull();
    }

    @Test
    void testSavePropertiesConfiguration() {
        service.savePropertiesConfiguration(propertiesConfiguration);

        verify(configurationRepository).saveConfiguration(ConfigurationType.PROPERTIES, propertiesConfiguration);
    }

    @Test
    void testLoadTranslatedRestrictedProperties() {
        when(configurationRepository.findByType(ConfigurationType.PROPERTIES, PropertiesConfiguration.class))
                .thenReturn(Optional.of(propertiesConfiguration));

        var result = service.loadTranslatedRestrictedProperties();

        assertThat(result).containsExactlyElementsOf(propertiesConfiguration.getCategories());
    }

    @Test
    void testIsDesktopProfileEnabled() {
        when(environment.matchesProfiles("desktop")).thenReturn(true);

        assertThat(service.isDesktopProfileEnabled()).isTrue();
    }

    @Test
    void testIsE2eProfileEnabled() {
        when(environment.matchesProfiles("e2e")).thenReturn(true);

        assertThat(service.isE2eProfileEnabled()).isTrue();
    }

    @Test
    void testLoadTranslatedAppearanceConfigurationFound() {
        AppearanceConfiguration config = new AppearanceConfiguration();
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadTranslatedAppearanceConfiguration();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void testLoadTranslatedAppearanceConfigurationNotFound() {
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.empty());

        var result = service.loadTranslatedAppearanceConfiguration();

        assertThat(result).isNotNull();
    }

    @Test
    void testSaveAppearanceConfiguration() {
        AppearanceConfiguration config = new AppearanceConfiguration();

        service.saveAppearanceConfiguration(config);

        verify(configurationRepository).saveConfiguration(ConfigurationType.APPEARANCE, config);
    }

    @Test
    void testLoadTagsConfigurationFound() {
        TagsConfiguration config = new TagsConfiguration();
        when(configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadTagsConfiguration();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void testLoadTagsConfigurationNotFound() {
        when(configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class))
                .thenReturn(Optional.empty());

        var result = service.loadTagsConfiguration();

        assertThat(result).isNotNull();
    }

    @Test
    void testLoadTranslatedRestrictedTagsConfiguration() {
        TagsConfiguration config = new TagsConfiguration();
        when(configurationRepository.findByType(ConfigurationType.TAGS, TagsConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadTranslatedRestrictedTagsConfiguration();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void testSaveTagsConfiguration() {
        TagsConfiguration config = new TagsConfiguration();

        service.saveTagsConfiguration(config);

        verify(configurationRepository).saveConfiguration(ConfigurationType.TAGS, config);
    }

    @Test
    void testLoadPeripheralConfigurationWindows() {
        System.setProperty("os.name", "Windows 10");
        PeripheralConfiguration config = new PeripheralConfiguration();
        when(configurationRepository.findByType(ConfigurationType.PERIPHERAL, PeripheralConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadPeripheralConfiguration();

        assertThat(result.getAvailableCameraPeripheralImplementations())
                .contains(PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL);
    }

    @Test
    void testLoadPeripheralConfigurationNonWindows() {
        System.setProperty("os.name", "Linux");
        PeripheralConfiguration config = new PeripheralConfiguration();
        when(configurationRepository.findByType(ConfigurationType.PERIPHERAL, PeripheralConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadPeripheralConfiguration();

        assertThat(result.getAvailableCameraPeripheralImplementations())
                .contains(PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL);
    }

    @Test
    void testSavePeripheralConfiguration() {
        PeripheralConfiguration config = new PeripheralConfiguration();

        service.savePeripheralConfiguration(config);

        verify(configurationRepository).saveConfiguration(ConfigurationType.PERIPHERAL, config);
        assertThat(config.getAvailableCameraPeripheralImplementations()).isEmpty();
    }

    @Test
    void testLoadExchangeConfigurationFound() {
        ExchangeConfiguration config = new ExchangeConfiguration();
        when(configurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class))
                .thenReturn(Optional.of(config));

        var result = service.loadExchangeConfiguration();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void testLoadExchangeConfigurationNotFound() {
        when(configurationRepository.findByType(ConfigurationType.EXCHANGE, ExchangeConfiguration.class))
                .thenReturn(Optional.empty());

        var result = service.loadExchangeConfiguration();

        assertThat(result).isNotNull();
    }

    @Test
    void testSaveExchangeConfiguration() {
        ExchangeConfiguration config = new ExchangeConfiguration();

        service.saveExchangeConfiguration(config);

        verify(configurationRepository).saveConfiguration(ConfigurationType.EXCHANGE, config);
    }

}
