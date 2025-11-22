package com.arassec.artivact.application.service.configuration;

import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.configuration.PeripheralsConfiguration;
import com.arassec.artivact.domain.model.peripheral.Peripheral;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestPeripheralsConfigurationServiceTest {

    @Mock
    private Peripheral peripheral;

    @Test
    void testTestConfig() {
        PeripheralConfig peripheralConfig = new PeripheralConfig();
        peripheralConfig.setId("test-id");
        peripheralConfig.setPeripheralImplementation(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        when(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        when(peripheral.getStatus(peripheralConfig)).thenReturn(PeripheralStatus.AVAILABLE);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        PeripheralStatus result = service.testConfig(peripheralConfig);

        assertThat(result).isEqualTo(PeripheralStatus.AVAILABLE);
    }

    @Test
    void testTestConfigWithError() {
        PeripheralConfig peripheralConfig = new PeripheralConfig();
        peripheralConfig.setId("test-id");
        peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);

        when(peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL)).thenReturn(true);
        when(peripheral.getStatus(peripheralConfig)).thenReturn(PeripheralStatus.ERROR);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        PeripheralStatus result = service.testConfig(peripheralConfig);

        assertThat(result).isEqualTo(PeripheralStatus.ERROR);
    }

    @Test
    void testTestConfigWithDisconnected() {
        PeripheralConfig peripheralConfig = new PeripheralConfig();
        peripheralConfig.setId("test-id");
        peripheralConfig.setPeripheralImplementation(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);

        when(peripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL)).thenReturn(true);
        when(peripheral.getStatus(peripheralConfig)).thenReturn(PeripheralStatus.DISCONNECTED);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        PeripheralStatus result = service.testConfig(peripheralConfig);

        assertThat(result).isEqualTo(PeripheralStatus.DISCONNECTED);
    }

    @Test
    void testTestConfigWithUnsupportedAdapter() {
        PeripheralConfig peripheralConfig = new PeripheralConfig();
        peripheralConfig.setId("test-id");
        peripheralConfig.setPeripheralImplementation(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        when(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(false);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        assertThatThrownBy(() -> service.testConfig(peripheralConfig))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Could not detect selected adapter!");
    }

    @Test
    void testTestConfigsWithMultiplePeripherals() {
        PeripheralConfig turntableConfig = new PeripheralConfig();
        turntableConfig.setId("turntable-id");
        turntableConfig.setPeripheralImplementation(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);

        PeripheralConfig cameraConfig = new PeripheralConfig();
        cameraConfig.setId("camera-id");
        cameraConfig.setPeripheralImplementation(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        PeripheralConfig imageConfig = new PeripheralConfig();
        imageConfig.setId("image-id");
        imageConfig.setPeripheralImplementation(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);

        PeripheralConfig modelCreatorConfig = new PeripheralConfig();
        modelCreatorConfig.setId("model-creator-id");
        modelCreatorConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);

        PeripheralConfig modelEditorConfig = new PeripheralConfig();
        modelEditorConfig.setId("model-editor-id");
        modelEditorConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .turntablePeripheralConfigs(List.of(turntableConfig))
                .cameraPeripheralConfigs(List.of(cameraConfig))
                .imageBackgroundRemovalPeripheralConfigs(List.of(imageConfig))
                .modelCreatorPeripheralConfigs(List.of(modelCreatorConfig))
                .modelEditorPeripheralConfigs(List.of(modelEditorConfig))
                .build();

        when(peripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL)).thenReturn(true);
        when(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        when(peripheral.supports(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL)).thenReturn(true);
        when(peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL)).thenReturn(true);
        when(peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL)).thenReturn(true);
        
        when(peripheral.getStatus(turntableConfig)).thenReturn(PeripheralStatus.AVAILABLE);
        when(peripheral.getStatus(cameraConfig)).thenReturn(PeripheralStatus.DISCONNECTED);
        when(peripheral.getStatus(imageConfig)).thenReturn(PeripheralStatus.ERROR);
        when(peripheral.getStatus(modelCreatorConfig)).thenReturn(PeripheralStatus.NOT_EXECUTABLE);
        when(peripheral.getStatus(modelEditorConfig)).thenReturn(PeripheralStatus.FILE_DOESNT_EXIST);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        Map<String, PeripheralStatus> result = service.testConfigs(peripheralsConfiguration);

        assertThat(result).hasSize(5);
        assertThat(result.get("turntable-id")).isEqualTo(PeripheralStatus.AVAILABLE);
        assertThat(result.get("camera-id")).isEqualTo(PeripheralStatus.DISCONNECTED);
        assertThat(result.get("image-id")).isEqualTo(PeripheralStatus.ERROR);
        assertThat(result.get("model-creator-id")).isEqualTo(PeripheralStatus.NOT_EXECUTABLE);
        assertThat(result.get("model-editor-id")).isEqualTo(PeripheralStatus.FILE_DOESNT_EXIST);
    }

    @Test
    void testTestConfigsWithEmptyConfiguration() {
        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .turntablePeripheralConfigs(List.of())
                .cameraPeripheralConfigs(List.of())
                .imageBackgroundRemovalPeripheralConfigs(List.of())
                .modelCreatorPeripheralConfigs(List.of())
                .modelEditorPeripheralConfigs(List.of())
                .build();

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(List.of(peripheral));

        Map<String, PeripheralStatus> result = service.testConfigs(peripheralsConfiguration);

        assertThat(result).isEmpty();
    }

    @Test
    void testTestConfigsWithNullPeripheralsInList() {
        PeripheralConfig cameraConfig = new PeripheralConfig();
        cameraConfig.setId("camera-id");
        cameraConfig.setPeripheralImplementation(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);

        PeripheralsConfiguration peripheralsConfiguration = PeripheralsConfiguration.builder()
                .cameraPeripheralConfigs(List.of(cameraConfig))
                .build();

        when(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).thenReturn(true);
        when(peripheral.getStatus(cameraConfig)).thenReturn(PeripheralStatus.AVAILABLE);

        TestPeripheralsConfigurationService service = new TestPeripheralsConfigurationService(Arrays.asList(null, peripheral));

        Map<String, PeripheralStatus> result = service.testConfigs(peripheralsConfiguration);

        assertThat(result).hasSize(1);
        assertThat(result.get("camera-id")).isEqualTo(PeripheralStatus.AVAILABLE);
    }

}
