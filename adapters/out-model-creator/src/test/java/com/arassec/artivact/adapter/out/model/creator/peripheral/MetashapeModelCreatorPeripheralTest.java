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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link MetashapeModelCreatorPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class MetashapeModelCreatorPeripheralTest {

    /**
     * Peripheral under test.
     */
    @InjectMocks
    private MetashapeModelCreatorPeripheral metashapeModelCreatorPeripheral;

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
     * Tests getting the supported peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(metashapeModelCreatorPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL);
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
                                PeripheralImplementation.METASHAPE_MODEL_CREATOR_PERIPHERAL,
                                "/path/to/metashape-executable"
                        ))
                        .build()
                )
                .build();

        metashapeModelCreatorPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        ModelCreationResult modelCreationResult = metashapeModelCreatorPeripheral.createModel(List.of(imagePath));

        assertThat(modelCreationResult.resultDir().toString()).endsWith(exportDir.toString());
        assertThat(modelCreationResult.comment()).isEqualTo("metashape");

        verify(fileRepository).emptyDir(workDir);
        verify(fileRepository).createDirIfRequired(argThat(s -> s.endsWith(exportDir)));

        verify(fileRepository).copy(imagePath, Path.of("workDir/image.jpg"), StandardCopyOption.REPLACE_EXISTING);

        verify(fileRepository).openDirInOs(workDir);
        verify(osGateway).execute("/path/to/metashape-executable", List.of());

        assertDoesNotThrow(() -> metashapeModelCreatorPeripheral.teardown());
    }

}
