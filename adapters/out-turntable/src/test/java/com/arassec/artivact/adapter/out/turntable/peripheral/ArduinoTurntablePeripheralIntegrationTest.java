package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for the  {@link ArduinoTurntablePeripheral}.
 */
@Disabled("Only for manual turntable tests!")
class ArduinoTurntablePeripheralIntegrationTest {

    /**
     * Tests a full rotation of the turntable.
     */
    @Test
    void testRotate() {
        ProgressMonitor progressMonitor = new ProgressMonitor("DefaultTurntablePeripheralIntegrationTest", "testRotate");
        PeripheralInitParams initParams = PeripheralInitParams.builder()
                .config(new ArduinoTurntablePeripheralConfig(100))
                .build();

        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral();

        peripheral.initialize(progressMonitor, initParams);

        peripheral.rotate(1);

        assertDoesNotThrow(peripheral::teardown);
    }

}
