package com.arassec.artivact.adapter.out.image.manipulation.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DefaultImageManipulationPeripheral}.
 */
class DefaultImageManipulationPeripheralTest {

    /**
     * Peripheral under test.
     */
    private final DefaultImageManipulationPeripheral defaultImageManipulationPeripheral = new DefaultImageManipulationPeripheral();

    /**
     * Tests the supported peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(defaultImageManipulationPeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL);
    }

    /**
     * Tests removing the background of an image.
     */
    @Test
    @SneakyThrows
    void testRemoveBackgrounds() {
        ProgressMonitor progressMonitor = new ProgressMonitor("DefaultImageManipulationPeripheralTest", "test");

        PeripheralConfiguration peripheralConfiguration = new PeripheralConfiguration();
        peripheralConfiguration.setConfigValues(Map.of(
                PeripheralImplementation.DEFAULT_IMAGE_MANIPULATION_PERIPHERAL, "silueta.onnx#input.1#320#320#5"
        ));

        PeripheralInitParams peripheralAdapterInitParams = PeripheralInitParams.builder()
                .projectRoot(Path.of("../../application/src/main/resources/project-setup"))
                .configuration(peripheralConfiguration)
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
