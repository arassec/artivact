package com.arassec.artivact.adapter.out.image.background.removal.peripheral;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.OnnxBackgroundRemovalPeripheralConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link OnnxImageBackgroundRemovalPeripheral}.
 */
class OnnxImageBackgroundRemovalPeripheralTest {

    /**
     * Peripheral under test.
     */
    private final OnnxImageBackgroundRemovalPeripheral defaultImageManipulationPeripheral =
            new OnnxImageBackgroundRemovalPeripheral(mock(FileRepository.class), mock(UseProjectDirsUseCase.class));

    /**
     * Tests the supported peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(defaultImageManipulationPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL);
    }

    /**
     * Tests removing the background of an image.
     */
    @Test
    @SneakyThrows
    void testRemoveBackgrounds() {
        ProgressMonitor progressMonitor = new ProgressMonitor("DefaultImageManipulationPeripheralTest", "test");

        PeripheralInitParams peripheralAdapterInitParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("../../application/src/main/resources/project-setup"))
                .config(new OnnxBackgroundRemovalPeripheralConfig("{projectDir}/utils/onnx/silueta.onnx", "input.1", 320, 320, 5))
                .workDir(Path.of("target"))
                .build();

        defaultImageManipulationPeripheral.initialize(progressMonitor, peripheralAdapterInitParams);

        Path filePath = Path.of("src/test/resources/background-test.jpg");

        defaultImageManipulationPeripheral.removeBackgrounds(List.of(filePath));

        defaultImageManipulationPeripheral.teardown();

        List<Path> result = defaultImageManipulationPeripheral.getModifiedImages();
        assertThat(result).hasSize(1);

        Path resultImage = result.getFirst();
        assertThat(resultImage).exists();
        assertThat(Files.size(resultImage)).isGreaterThan(0);

        Files.delete(resultImage);
    }

}
