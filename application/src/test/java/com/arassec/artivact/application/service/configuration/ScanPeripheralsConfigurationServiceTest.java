package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.application.port.in.configuration.LoadPeripheralsConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.SavePeripheralConfigurationUseCase;
import com.arassec.artivact.application.port.in.operation.RunBackgroundOperationUseCase;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.operation.BackgroundOperation;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanPeripheralsConfigurationServiceTest {

    private ScanPeripheralsConfigurationService service;

    @Mock
    private RunBackgroundOperationUseCase runBackgroundOperationUseCase;

    @Mock
    private LoadPeripheralsConfigurationUseCase loadPeripheralsConfigurationUseCase;

    @Mock
    private SavePeripheralConfigurationUseCase savePeripheralConfigurationUseCase;

    @Mock
    private Peripheral turntablePeripheral;

    @Mock
    private Peripheral cameraPeripheral;

    @Mock
    private Peripheral backgroundRemovalPeripheral;

    @Mock
    private Peripheral modelCreatorPeripheral;

    @Mock
    private Peripheral modelEditorPeripheral;

    @BeforeEach
    void setUp() {
        List<Peripheral> peripherals = new ArrayList<>();
        peripherals.add(turntablePeripheral);
        peripherals.add(cameraPeripheral);
        peripherals.add(backgroundRemovalPeripheral);
        peripherals.add(modelCreatorPeripheral);
        peripherals.add(modelEditorPeripheral);

        service = new ScanPeripheralsConfigurationService(
                runBackgroundOperationUseCase,
                loadPeripheralsConfigurationUseCase,
                savePeripheralConfigurationUseCase,
                peripherals
        );
    }

    @Test
    void scanPeripheralsConfigurationScansAllPeripheralTypesWhenEmpty() {
        PeripheralsConfiguration emptyConfig = PeripheralsConfiguration.builder()
                .turntablePeripheralConfigs(new ArrayList<>())
                .cameraPeripheralConfigs(new ArrayList<>())
                .imageBackgroundRemovalPeripheralConfigs(new ArrayList<>())
                .modelCreatorPeripheralConfigs(new ArrayList<>())
                .modelEditorPeripheralConfigs(new ArrayList<>())
                .build();

        when(loadPeripheralsConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(emptyConfig);

        PeripheralConfig turntableConfig = mock(PeripheralConfig.class);
        PeripheralConfig cameraConfig = mock(PeripheralConfig.class);
        PeripheralConfig bgRemovalConfig = mock(PeripheralConfig.class);
        PeripheralConfig modelCreatorConfig = mock(PeripheralConfig.class);
        PeripheralConfig modelEditorConfig = mock(PeripheralConfig.class);

        lenient().when(turntablePeripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL)).thenReturn(true);
        lenient().when(turntablePeripheral.scanPeripherals()).thenReturn(List.of(turntableConfig));

        lenient().when(cameraPeripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        lenient().when(cameraPeripheral.scanPeripherals()).thenReturn(List.of(cameraConfig));

        lenient().when(backgroundRemovalPeripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL)).thenReturn(true);
        lenient().when(backgroundRemovalPeripheral.scanPeripherals()).thenReturn(List.of(bgRemovalConfig));

        lenient().when(modelCreatorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL)).thenReturn(true);
        lenient().when(modelCreatorPeripheral.scanPeripherals()).thenReturn(List.of(modelCreatorConfig));

        lenient().when(modelEditorPeripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL)).thenReturn(true);
        lenient().when(modelEditorPeripheral.scanPeripherals()).thenReturn(List.of(modelEditorConfig));

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service.scanPeripheralsConfiguration();

        verify(savePeripheralConfigurationUseCase).savePeripheralConfiguration(emptyConfig);
        assertThat(emptyConfig.getTurntablePeripheralConfigs()).containsExactly(turntableConfig);
        assertThat(emptyConfig.getCameraPeripheralConfigs()).containsExactly(cameraConfig);
        assertThat(emptyConfig.getImageBackgroundRemovalPeripheralConfigs()).containsExactly(bgRemovalConfig);
        assertThat(emptyConfig.getModelCreatorPeripheralConfigs()).containsExactly(modelCreatorConfig);
        assertThat(emptyConfig.getModelEditorPeripheralConfigs()).containsExactly(modelEditorConfig);
    }

    @Test
    void scanPeripheralsConfigurationSkipsExistingConfigs() {
        PeripheralConfig existingTurntableConfig = mock(PeripheralConfig.class);
        PeripheralConfig existingCameraConfig = mock(PeripheralConfig.class);
        PeripheralConfig existingBgRemovalConfig = mock(PeripheralConfig.class);
        PeripheralConfig existingModelCreatorConfig = mock(PeripheralConfig.class);
        PeripheralConfig existingModelEditorConfig = mock(PeripheralConfig.class);

        PeripheralsConfiguration existingConfig = PeripheralsConfiguration.builder()
                .turntablePeripheralConfigs(new ArrayList<>(List.of(existingTurntableConfig)))
                .cameraPeripheralConfigs(new ArrayList<>(List.of(existingCameraConfig)))
                .imageBackgroundRemovalPeripheralConfigs(new ArrayList<>(List.of(existingBgRemovalConfig)))
                .modelCreatorPeripheralConfigs(new ArrayList<>(List.of(existingModelCreatorConfig)))
                .modelEditorPeripheralConfigs(new ArrayList<>(List.of(existingModelEditorConfig)))
                .build();

        when(loadPeripheralsConfigurationUseCase.loadPeripheralConfiguration()).thenReturn(existingConfig);

        doAnswer(invocation -> {
            BackgroundOperation backgroundOperation = invocation.getArgument(2);
            backgroundOperation.execute(new ProgressMonitor("test", "test"));
            return null;
        }).when(runBackgroundOperationUseCase).execute(any(), any(), any());

        service.scanPeripheralsConfiguration();

        verify(turntablePeripheral, never()).scanPeripherals();
        verify(cameraPeripheral, never()).scanPeripherals();
        verify(backgroundRemovalPeripheral, never()).scanPeripherals();
        verify(modelCreatorPeripheral, never()).scanPeripherals();
        verify(modelEditorPeripheral, never()).scanPeripherals();

        verify(savePeripheralConfigurationUseCase).savePeripheralConfiguration(existingConfig);
    }
}
