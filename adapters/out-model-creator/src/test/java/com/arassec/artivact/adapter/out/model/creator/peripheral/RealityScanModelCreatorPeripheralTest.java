package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link RealityScanModelCreatorPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class RealityScanModelCreatorPeripheralTest {

    /**
     * Peripheral under test.
     */
    @InjectMocks
    private RealityScanModelCreatorPeripheral realityScanModelCreatorPeripheral;

    /**
     * The file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Gateway to the operating system.
     */
    @Mock
    private OsGateway osGateway;

    /**
     * Captor for command parameters.
     */
    @Captor
    private ArgumentCaptor<List<String>> commandArgsCaptor;

    /**
     * Tests getting the supported peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(realityScanModelCreatorPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL);
    }

    /**
     * Tests creating a 3D model.
     */
    @Test
    void testCreateModel() {
        Path workDir = Path.of("workDir");
        Path imagePath = Path.of("image.jpg");
        Path exportDir = Path.of("workDir/export");

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("projectRoot"))
                .workDir(workDir)
                .configuration(PeripheralConfiguration.builder()
                        .configValues(Map.of(
                                PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL,
                                "/path/to/realityscan-executable"
                        ))
                        .build()
                )
                .build();

        realityScanModelCreatorPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        ModelCreationResult modelCreationResult = realityScanModelCreatorPeripheral.createModel(List.of(imagePath));

        assertThat(modelCreationResult.resultDir().toString()).endsWith(exportDir.toString());
        assertThat(modelCreationResult.comment()).isEqualTo("RealityScan");

        verify(fileRepository).emptyDir(workDir);
        verify(fileRepository).createDirIfRequired(argThat(s -> s.endsWith(exportDir)));

        verify(fileRepository).copy(imagePath, Path.of("workDir/image.jpg"), StandardCopyOption.REPLACE_EXISTING);

        verify(osGateway).execute(eq("/path/to/realityscan-executable"), commandArgsCaptor.capture());

        List<String> commandArgs = commandArgsCaptor.getValue();
        assertThat(commandArgs).hasSize(4);
        assertThat(commandArgs.get(0)).isEqualTo("-addFolder");
        assertThat(commandArgs.get(1)).endsWith(workDir.toString());
        assertThat(commandArgs.get(2)).isEqualTo("-save");
        assertThat(commandArgs.get(3)).endsWith(workDir + "/MyProject.rcproj");

        assertDoesNotThrow(() -> realityScanModelCreatorPeripheral.teardown());
    }

    /**
     * Tests the headless model creation configuration.
     */
    @Test
    void testCreateModelHeadless() {
        Path workDir = Path.of("workDir");
        Path imagePath = Path.of("image.jpg");
        Path exportDir = Path.of("workDir/export");

        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("projectRoot"))
                .workDir(workDir)
                .configuration(PeripheralConfiguration.builder()
                        .configValues(Map.of(
                                PeripheralImplementation.REALITY_SCAN_MODEL_CREATOR_PERIPHERAL,
                                "/path/to/realityscan-executable#headless#12345"
                        ))
                        .build()
                )
                .build();

        realityScanModelCreatorPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        ModelCreationResult modelCreationResult = realityScanModelCreatorPeripheral.createModel(List.of(imagePath));

        assertThat(modelCreationResult.resultDir().toString()).endsWith(exportDir.toString());
        assertThat(modelCreationResult.comment()).isEqualTo("RealityScan");

        verify(osGateway).execute(eq("/path/to/realityscan-executable"), commandArgsCaptor.capture());

        List<String> commandArgs = commandArgsCaptor.getValue();
        assertThat(commandArgs).hasSize(15);
        assertThat(commandArgs.get(0)).isEqualTo("-addFolder");
        assertThat(commandArgs.get(1)).endsWith(workDir.toString());
        assertThat(commandArgs.get(2)).isEqualTo("-save");
        assertThat(commandArgs.get(3)).endsWith(workDir + "/MyProject.rcproj");
        assertThat(commandArgs.get(4)).isEqualTo("-headless");
        assertThat(commandArgs.get(5)).isEqualTo("-align");
        assertThat(commandArgs.get(6)).isEqualTo("-setReconstructionRegionAuto");
        assertThat(commandArgs.get(7)).isEqualTo("-calculateNormalModel");
        assertThat(commandArgs.get(8)).isEqualTo("-simplify");
        assertThat(commandArgs.get(9)).isEqualTo("12345");
        assertThat(commandArgs.get(10)).isEqualTo("-calculateTexture");
        assertThat(commandArgs.get(11)).isEqualTo("-exportSelectedModel");
        assertThat(commandArgs.get(12)).endsWith(exportDir + "/RealityScanExport.obj");
        assertThat(commandArgs.get(13)).endsWith("utils/RealityScan/realityscan-export-settings.xml");
        assertThat(commandArgs.get(14)).isEqualTo("-quit");
    }

}
