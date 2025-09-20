package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralConfiguration;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for the  {@link DefaultTurntablePeripheral}.
 */
@Disabled("Only for manual turntable tests!")
class DefaultTurntablePeripheralIntegrationTest {

    /**
     * Tests a full rotation of the turntable.
     */
    @Test
    void testRotate() {
        ProgressMonitor progressMonitor = new ProgressMonitor("DefaultTurntablePeripheralIntegrationTest", "testRotate");
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .configuration(PeripheralConfiguration.builder()
                        .configValues(Map.of(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL, "100"))
                        .build())
                .build();

        DefaultTurntablePeripheral peripheral = new DefaultTurntablePeripheral();

        peripheral.initialize(progressMonitor, initParams);

        peripheral.rotate(1);

        assertDoesNotThrow(peripheral::teardown);
    }

}
