package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SavePeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ScanPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for peripheral configuration scanning.
 */
@Service
@RequiredArgsConstructor
public class ScanPeripheralsConfigurationService implements ScanPeripheralsConfigurationUseCase {

    private final RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    private final LoadPeripheralsConfigurationUseCase loadPeripheralsConfigurationUseCase;

    private final SavePeripheralConfigurationUseCase savePeripheralConfigurationUseCase;

    /**
     * List of all available adapters.
     */
    private final List<Peripheral> peripherals;

    /**
     * {@inheritDoc}
     */
    @Override
    public void scanPeripheralsConfiguration() {

        runBackgroundOperationUseCase.execute("scanPeripherals", "init", progressMonitor -> {
            PeripheralsConfiguration peripheralsConfiguration = loadPeripheralsConfigurationUseCase.loadPeripheralConfiguration();

            if (peripheralsConfiguration.getTurntablePeripheralConfigs().isEmpty()) {
                progressMonitor.updateProgress(1, 5);
                progressMonitor.updateLabelKey("scanTurntables");
                List<PeripheralConfig> configs = peripherals.stream()
                        .filter(peripheral -> peripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL))
                        .map(Peripheral::scanPeripherals)
                        .flatMap(List::stream)
                        .toList();
                peripheralsConfiguration.setTurntablePeripheralConfigs(configs);
            }

            if (peripheralsConfiguration.getCameraPeripheralConfigs().isEmpty()) {
                progressMonitor.updateProgress(2, 5);
                progressMonitor.updateLabelKey("scanCameras");
                List<PeripheralConfig> configs = peripherals.stream()
                        .filter(peripheral -> peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL) || peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL))
                        .map(Peripheral::scanPeripherals)
                        .flatMap(List::stream)
                        .toList();
                peripheralsConfiguration.setCameraPeripheralConfigs(configs);
            }

            if (peripheralsConfiguration.getImageBackgroundRemovalPeripheralConfigs().isEmpty()) {
                progressMonitor.updateProgress(3, 5);
                progressMonitor.updateLabelKey("scanImageBackroundRemovers");
                List<PeripheralConfig> configs = peripherals.stream()
                        .filter(peripheral -> peripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL))
                        .map(Peripheral::scanPeripherals)
                        .flatMap(List::stream)
                        .toList();
                peripheralsConfiguration.setImageBackgroundRemovalPeripheralConfigs(configs);
            }

            if (peripheralsConfiguration.getModelCreatorPeripheralConfigs().isEmpty()) {
                progressMonitor.updateProgress(4, 5);
                progressMonitor.updateLabelKey("scanModelCreators");
                List<PeripheralConfig> configs = peripherals.stream()
                        .filter(peripheral -> peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL))
                        .map(Peripheral::scanPeripherals)
                        .flatMap(List::stream)
                        .toList();
                peripheralsConfiguration.setModelCreatorPeripheralConfigs(configs);
            }

            if (peripheralsConfiguration.getModelEditorPeripheralConfigs().isEmpty()) {
                progressMonitor.updateProgress(5, 5);
                progressMonitor.updateLabelKey("scanModelEditors");
                List<PeripheralConfig> configs = peripherals.stream()
                        .filter(peripheral -> peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL))
                        .map(Peripheral::scanPeripherals)
                        .flatMap(List::stream)
                        .toList();
                peripheralsConfiguration.setModelEditorPeripheralConfigs(configs);
            }

            savePeripheralConfigurationUseCase.savePeripheralConfiguration(peripheralsConfiguration);
        });
    }

}
