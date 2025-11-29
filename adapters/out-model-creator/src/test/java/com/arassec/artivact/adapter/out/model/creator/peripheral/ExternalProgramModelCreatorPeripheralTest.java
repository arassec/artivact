package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ModelCreatorPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalProgramModelCreatorPeripheralTest {

    @InjectMocks
    private ExternalProgramModelCreatorPeripheral peripheral;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private OsGateway osGateway;

    @Test
    void getSupportedImplementationReturnsExternalProgramModelCreatorPeripheral() {
        assertThat(peripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
    }

    @Test
    void getStatusReturnsAvailableWhenExecutable() {
        ModelCreatorPeripheralConfig config = new ModelCreatorPeripheralConfig();
        config.setCommand("/usr/bin/meshroom");

        when(osGateway.isExecutable("/usr/bin/meshroom")).thenReturn(true);

        PeripheralStatus status = peripheral.getStatus(config);

        assertThat(status).isEqualTo(PeripheralStatus.AVAILABLE);
    }

    @Test
    void getStatusReturnsNotExecutableWhenNotExecutable() {
        ModelCreatorPeripheralConfig config = new ModelCreatorPeripheralConfig();
        config.setCommand("/usr/bin/nonexistent");

        when(osGateway.isExecutable("/usr/bin/nonexistent")).thenReturn(false);

        PeripheralStatus status = peripheral.getStatus(config);

        assertThat(status).isEqualTo(PeripheralStatus.NOT_EXECUTABLE);
    }

    @Test
    void scanPeripheralsFindsMesshroomOnLinux() {
        Path homePath = Path.of("/home/user");
        Path meshroomPath = Path.of("/home/user/Meshroom-2025");

        when(osGateway.scanForDirectory(homePath, 5, "Meshroom-2025")).thenReturn(Optional.of(meshroomPath));
        when(osGateway.isLinux()).thenReturn(true);
        when(osGateway.getUserHomeDirectory()).thenReturn(homePath);

        List<PeripheralConfig> result = peripheral.scanPeripherals();

        assertThat(result.stream()
                .filter(c -> c.getLabel().contains("Meshroom"))
                .count()).isGreaterThan(0);
    }

    @Test
    void scanPeripheralsReturnsEmptyWhenNoSupportedProgramsFound() {
        when(osGateway.scanForDirectory(any(), anyInt(), anyString())).thenReturn(Optional.empty());
        when(osGateway.isLinux()).thenReturn(false);
        when(osGateway.isWindows()).thenReturn(false);

        List<PeripheralConfig> result = peripheral.scanPeripherals();

        assertThat(result).isEmpty();
    }

    @Test
    void createModelCopiesImagesAndExecutesCommand() {
        Path tempDir = Path.of("/project/temp");
        Path projectRoot = Path.of("/project");
        Path resultDir = Path.of("/project/temp/export/");

        ModelCreatorPeripheralConfig config = new ModelCreatorPeripheralConfig();
        config.setCommand("/usr/bin/meshroom");
        config.setArguments("-i {projectDir}/temp/\n-p photogrammetry");
        config.setResultDir("{projectDir}/temp/export/");

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .config(config)
                .workDir(tempDir)
                .projectRoot(projectRoot)
                .build();

        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");
        peripheral.initialize(progressMonitor, initParams);

        List<Path> images = List.of(Path.of("/project/images/001.jpg"), Path.of("/project/images/002.jpg"));

        when(osGateway.execute(eq("/usr/bin/meshroom"), anyList())).thenReturn(true);

        ModelCreationResult result = peripheral.createModel(images);

        assertThat(result.resultDir()).isEqualTo(resultDir);
        assertThat(result.comment()).isEqualTo(config.getLabel());

        verify(fileRepository).emptyDir(tempDir);
        verify(fileRepository, atLeastOnce()).copy(any(Path.class), any(), any());
        verify(fileRepository).emptyDir(resultDir);
        verify(fileRepository).createDirIfRequired(resultDir);
    }

    @Test
    void createModelOpensInputDirWhenConfigured() {
        Path tempDir = Path.of("/project/temp");
        Path projectRoot = Path.of("/project");

        ModelCreatorPeripheralConfig config = new ModelCreatorPeripheralConfig();
        config.setCommand("/usr/bin/meshroom");
        config.setArguments("");
        config.setResultDir("{projectDir}/temp/export/");
        config.setOpenInputDirInOs(true);

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .config(config)
                .workDir(tempDir)
                .projectRoot(projectRoot)
                .build();

        ProgressMonitor progressMonitor = new ProgressMonitor("test", "test");
        peripheral.initialize(progressMonitor, initParams);

        List<Path> images = List.of(Path.of("/project/images/001.jpg"));

        when(osGateway.execute(anyString(), anyList())).thenReturn(true);

        peripheral.createModel(images);

        verify(fileRepository).openDirInOs(tempDir);
    }

    @Test
    void supportsReturnsTrueForExternalProgramModelCreatorPeripheral() {
        assertThat(peripheral.supports(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL)).isTrue();
    }

    @Test
    void supportsReturnsFalseForOtherImplementations() {
        assertThat(peripheral.supports(PeripheralImplementation.PTP_CAMERA_PERIPHERAL)).isFalse();
        assertThat(peripheral.supports(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL)).isFalse();
    }
}
