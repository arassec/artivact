package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arasse.jptp.main.ImageCaptureDevice;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.PtpCameraPeripheralConfig;
import com.arassec.jptp.core.datatype.complex.DataObject;
import com.arassec.jptp.core.datatype.complex.DeviceInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PtpCameraPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class PtpCameraPeripheralTest {

    /**
     * The peripheral under test.
     */
    @InjectMocks
    private PtpCameraPeripheral defaultCameraPeripheral;

    /**
     * Image capture device.
     */
    @Mock
    private ImageCaptureDevice imageCaptureDevice;

    /**
     * The file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Tests getting the supported implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(defaultCameraPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.PTP_CAMERA_PERIPHERAL);
    }

    /**
     * Tests image capturing.
     */
    @Test
    void testCaptureImage() {
        when(imageCaptureDevice.initialize(Duration.ofSeconds(15), Duration.ofSeconds(15))).thenReturn(true);
        when(imageCaptureDevice.getDeviceInfo()).thenReturn(Optional.of(mock(DeviceInfo.class)));

        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        PeripheralInitParams peripheralInitParams = mock(PeripheralInitParams.class);
        when(peripheralInitParams.getConfig()).thenReturn(new PtpCameraPeripheralConfig());

        // Test initialization:
        defaultCameraPeripheral.initialize(progressMonitor, peripheralInitParams);

        // Test image capturing:
        when(imageCaptureDevice.captureImage()).thenReturn(Optional.of(mock(DataObject.class)));

        Path targetFile = Path.of("test.jpg");
        assertThat(defaultCameraPeripheral.captureImage(targetFile)).isTrue();
        verify(fileRepository).saveFile(targetFile, null); // data is null due to mocked DataObject!

        // Test teardown:
        defaultCameraPeripheral.teardown();
        verify(imageCaptureDevice).teardown();
    }

    /**
     * Tests error handling during image capturing.
     */
    @Test
    void testImageCaptureFail() {
        when(imageCaptureDevice.captureImage()).thenReturn(Optional.empty());
        Path imagePath = Path.of("test.jpg");
        assertThrows(ArtivactException.class, () -> defaultCameraPeripheral.captureImage(imagePath));
    }

    @Test
    void testInitializeFail() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(false);

        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        PeripheralInitParams peripheralInitParams = mock(PeripheralInitParams.class);
        when(peripheralInitParams.getConfig()).thenReturn(new PtpCameraPeripheralConfig());

        assertThrows(ArtivactException.class, () -> defaultCameraPeripheral.initialize(progressMonitor, peripheralInitParams));
    }

    @Test
    void testGetStatusInUse() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(true);
        when(imageCaptureDevice.getDeviceInfo()).thenReturn(Optional.of(mock(DeviceInfo.class)));

        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        PeripheralInitParams peripheralInitParams = mock(PeripheralInitParams.class);
        when(peripheralInitParams.getConfig()).thenReturn(new PtpCameraPeripheralConfig());

        defaultCameraPeripheral.initialize(progressMonitor, peripheralInitParams);

        assertThat(defaultCameraPeripheral.getStatus(null)).isEqualTo(PeripheralStatus.AVAILABLE);
    }

    @Test
    void testGetStatusAvailable() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(true);

        assertThat(defaultCameraPeripheral.getStatus(null)).isEqualTo(PeripheralStatus.AVAILABLE);

        verify(imageCaptureDevice).teardown();
    }

    @Test
    void testGetStatusDisconnected() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(false);

        assertThat(defaultCameraPeripheral.getStatus(null)).isEqualTo(PeripheralStatus.DISCONNECTED);
    }

    @Test
    void testScanPeripheralsInUse() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(true);
        when(imageCaptureDevice.getDeviceInfo()).thenReturn(Optional.of(mock(DeviceInfo.class)));

        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        PeripheralInitParams peripheralInitParams = mock(PeripheralInitParams.class);
        when(peripheralInitParams.getConfig()).thenReturn(new PtpCameraPeripheralConfig());

        defaultCameraPeripheral.initialize(progressMonitor, peripheralInitParams);

        assertThat(defaultCameraPeripheral.scanPeripherals()).isEmpty();
    }

    @Test
    void testScanPeripheralsFound() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(true);

        var result = defaultCameraPeripheral.scanPeripherals();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(PtpCameraPeripheralConfig.class);

        verify(imageCaptureDevice).teardown();
    }

    @Test
    void testScanPeripheralsNotFound() {
        when(imageCaptureDevice.initialize(any(Duration.class), any(Duration.class))).thenReturn(false);

        assertThat(defaultCameraPeripheral.scanPeripherals()).isEmpty();
    }

}
