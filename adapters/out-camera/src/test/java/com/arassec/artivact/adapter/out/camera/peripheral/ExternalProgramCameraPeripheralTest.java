package com.arassec.artivact.adapter.out.camera.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalProgramCameraPeripheralTest {

    @InjectMocks
    private ExternalProgramCameraPeripheral peripheral;

    @Mock
    private OsGateway osGateway;

    @Test
    void getSupportedImplementationReturnsExternalProgramCameraPeripheral() {
        assertThat(peripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
    }

    @Test
    void getStatusReturnsAvailableWhenExecutable() {
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/usr/bin/gphoto2");

        when(osGateway.isExecutable("/usr/bin/gphoto2")).thenReturn(true);

        PeripheralStatus status = peripheral.getStatus(config);

        assertThat(status).isEqualTo(PeripheralStatus.AVAILABLE);
    }

    @Test
    void getStatusReturnsNotExecutableWhenNotExecutable() {
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/usr/bin/nonexistent");

        when(osGateway.isExecutable("/usr/bin/nonexistent")).thenReturn(false);

        PeripheralStatus status = peripheral.getStatus(config);

        assertThat(status).isEqualTo(PeripheralStatus.NOT_EXECUTABLE);
    }

    @Test
    void scanPeripheralsReturnsEmptyListWhenNoSupportedProgramsFound() {
        when(osGateway.isLinux()).thenReturn(false);
        when(osGateway.isWindows()).thenReturn(false);

        List<PeripheralConfig> result = peripheral.scanPeripherals();

        assertThat(result).isEmpty();
    }

    @Test
    void scanPeripheralsReturnsGPhoto2ConfigOnLinuxWhenAvailable() {
        when(osGateway.isLinux()).thenReturn(true);
        when(osGateway.isExecutable("/usr/bin/gphoto2")).thenReturn(true);

        List<PeripheralConfig> result = peripheral.scanPeripherals();

        assertThat(result).hasSize(1);
        ExternalProgramPeripheralConfig config = (ExternalProgramPeripheralConfig) result.getFirst();
        assertThat(config.getLabel()).isEqualTo("gphoto2 Camera");
        assertThat(config.getCommand()).contains("gphoto2");
        assertThat(config.getPeripheralImplementation()).isEqualTo(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
    }

    @Test
    void scanPeripheralsReturnsDigiCamControlConfigOnWindowsWhenAvailable() {
        when(osGateway.isLinux()).thenReturn(false);
        when(osGateway.isWindows()).thenReturn(true);
        when(osGateway.isExecutable("C:\\Program Files (x86)\\digiCamControl\\CameraControl.exe")).thenReturn(true);

        List<PeripheralConfig> result = peripheral.scanPeripherals();

        assertThat(result).hasSize(1);
        ExternalProgramPeripheralConfig config = (ExternalProgramPeripheralConfig) result.getFirst();
        assertThat(config.getLabel()).isEqualTo("DigiCamControl Camera");
        assertThat(config.getCommand()).contains("CameraControl.exe");
        assertThat(config.getPeripheralImplementation()).isEqualTo(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL);
    }

    @Test
    void captureImageExecutesCommandAndReturnsTrue() {
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/usr/bin/gphoto2");
        config.setArguments("--filename {targetFile}\n--capture-image-and-download");

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .config(config)
                .build();

        peripheral.initialize(new ProgressMonitor("test", "test"), initParams);

        Path targetFile = Path.of("/path/to/image.jpg");
        when(osGateway.execute(eq("/usr/bin/gphoto2"), anyList())).thenReturn(true);

        boolean result = peripheral.captureImage(targetFile);

        assertThat(result).isTrue();
    }

    @Test
    void captureImageThrowsExceptionWhenCommandFails() {
        ExternalProgramPeripheralConfig config = new ExternalProgramPeripheralConfig();
        config.setCommand("/usr/bin/gphoto2");
        config.setArguments("--filename {targetFile}\n--capture-image-and-download");

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .config(config)
                .build();

        peripheral.initialize(new ProgressMonitor("test", "test"), initParams);

        Path targetFile = Path.of("/path/to/image.jpg");
        when(osGateway.execute(eq("/usr/bin/gphoto2"), anyList())).thenReturn(false);

        assertThatThrownBy(() -> peripheral.captureImage(targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not capture image");
    }

    @Test
    void supportsReturnsTrueForExternalProgramCameraPeripheral() {
        assertThat(peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_CAMERA_PERIPHERAL)).isTrue();
    }

    @Test
    void supportsReturnsFalseForOtherImplementations() {
        assertThat(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).isFalse();
        assertThat(peripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL)).isFalse();
    }
}
