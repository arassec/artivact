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
 * Tests the {@link GPhotoTwoCameraPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class GPhotoTwoCameraPeripheralTest {

    /**
     * Peripheral under test.
     */
    @InjectMocks
    private GPhotoTwoCameraPeripheral gPhotoTwoCameraPeripheral;

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
        assertThat(gPhotoTwoCameraPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL);
    }

    /**
     * Tests image capturing.
     */
    @Test
    void testCaptureImage() {
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .configuration(PeripheralConfiguration.builder()
                        .configValues(Map.of(
                                PeripheralImplementation.GPHOTO_TWO_CAMERA_PERIPHERAL,
                                "/path/to/gphoto2"
                        ))
                        .build()
                )
                .build();

        gPhotoTwoCameraPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        assertThat(gPhotoTwoCameraPeripheral.captureImage(Path.of("test.jpg"))).isTrue();

        verify(osGateway).execute("/path/to/gphoto2", List.of("--filename", "test.jpg", "--capture-image-and-download"));

        assertDoesNotThrow(() -> gPhotoTwoCameraPeripheral.teardown());
    }

}
