package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link DigiCamControlCameraPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class DigiCamControlCameraPeripheralTest {

    /**
     * The peripheral under test.
     */
    @InjectMocks
    private DigiCamControlCameraPeripheral digiCamControlCameraPeripheral;

    /**
     * Gateway to the operating system.
     */
    @Mock
    private OsGateway osGateway;

    /**
     * Tests getting the supported implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(digiCamControlCameraPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL);
    }

    /**
     * Tests image capturing.
     */
    @Test
    void testCaptureImage() {
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .configuration(PeripheralConfiguration.builder()
                        .configValues(Map.of(
                                PeripheralImplementation.DIGI_CAM_CONTROL_CAMERA_PERIPHERAL,
                                "C:\\path\\to\\digiCamControl.exe"
                        ))
                        .build()
                )
                .build();

        digiCamControlCameraPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        assertThat(digiCamControlCameraPeripheral.captureImage(Path.of("test.jpg"))).isTrue();

        verify(osGateway).execute("C:\\path\\to\\digiCamControl.exe", List.of("/filename", "test.jpg", "/capture"));

        assertDoesNotThrow(() -> digiCamControlCameraPeripheral.teardown());
    }

}
