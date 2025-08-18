package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.repository.FileRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link FallbackModelCreatorPeripheral}.
 */
@ExtendWith(MockitoExtension.class)
class FallbackModelCreatorPeripheralTest {

    /**
     * Peripheral under test.
     */
    @InjectMocks
    private FallbackModelCreatorPeripheral fallbackModelCreatorPeripheral;

    /**
     * The file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Tests getting the supported peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(fallbackModelCreatorPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.FALLBACK_MODEL_CREATOR_PERIPHERAL);
    }

    /**
     * Tests creating a 3D model.
     */
    @Test
    void testCreateModel() {
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .workDir(Path.of("test"))
                .build();

        fallbackModelCreatorPeripheral.initialize(mock(ProgressMonitor.class), initParams);

        ModelCreationResult modelCreationResult = fallbackModelCreatorPeripheral.createModel(List.of());

        Path exportDir = Path.of("test/export");

        assertThat(modelCreationResult.resultDir()).isEqualTo(exportDir);
        assertThat(modelCreationResult.comment()).isEqualTo("fallback");

        verify(fileRepository).emptyDir(exportDir);
        verify(fileRepository).createDirIfRequired(exportDir);

        assertDoesNotThrow(() -> fallbackModelCreatorPeripheral.teardown());
    }

}
