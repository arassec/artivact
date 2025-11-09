package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.TestPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.out.peripheral.TurntablePeripheral;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for testing peripheral configs.
 */
@Service
@RequiredArgsConstructor
public class TestPeripheralsConfigurationService implements TestPeripheralsConfigurationUseCase {

    /**
     * List of all available adapters.
     */
    private final List<Peripheral> peripheralAdapters;

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus testConfig(PeripheralConfig peripheralConfig) {
        Peripheral peripheral = getPeripheral(peripheralConfig.getPeripheralImplementation());
        return peripheral.getStatus(peripheralConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, PeripheralStatus> testConfigs(PeripheralsConfiguration peripheralsConfiguration) {
        Map<String, PeripheralStatus> status = new HashMap<>();

        peripheralsConfiguration.getTurntablePeripheralConfigs()
                .forEach(peripheralConfig -> addStatus(peripheralConfig, status));
        peripheralsConfiguration.getCameraPeripheralConfigs()
                .forEach(peripheralConfig -> addStatus(peripheralConfig, status));
        peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs()
                .forEach(peripheralConfig -> addStatus(peripheralConfig, status));
        peripheralsConfiguration.getModelCreatorPeripheralConfigs()
                .forEach(peripheralConfig -> addStatus(peripheralConfig, status));
        peripheralsConfiguration.getModelEditorPeripheralConfigs()
                .forEach(peripheralConfig -> addStatus(peripheralConfig, status));

        return status;
    }

    /**
     * Adds the status for the provided peripheral.
     *
     * @param peripheralConfig THe peripheral's config.
     * @param status           The result map containing all status.
     */
    private void addStatus(PeripheralConfig peripheralConfig, Map<String, PeripheralStatus> status) {
        Peripheral peripheral = getPeripheral(peripheralConfig.getPeripheralImplementation());
        status.put(peripheralConfig.getId(), peripheral.getStatus(peripheralConfig));
    }

    /**
     * Returns the desired peripheral.
     *
     * @param adapterImplementation The peripheral implementation to use.
     * @return The configured {@link TurntablePeripheral}.
     */
    private Peripheral getPeripheral(PeripheralImplementation adapterImplementation) {
        return peripheralAdapters.stream()
                .filter(Objects::nonNull)
                .filter(adapter -> adapter.supports(adapterImplementation))
                .findAny()
                .orElseThrow(() -> new ArtivactException("Could not detect selected adapter!"));
    }

}
