package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.PeripheralInitParams;
import com.arassec.artivact.domain.model.peripheral.configs.ArduinoTurntablePeripheralConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the  {@link ArduinoTurntablePeripheral}.
 */
@Disabled("Only for manual turntable tests under Linux!")
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

        OsGateway osGateway = mock(OsGateway.class);
        when(osGateway.isLinux()).thenReturn(true);

        ArduinoTurntablePeripheral peripheral = new ArduinoTurntablePeripheral(osGateway);

        peripheral.initialize(progressMonitor, initParams);

        peripheral.rotate(4);
        peripheral.rotate(4);
        peripheral.rotate(4);
        peripheral.rotate(4);

        assertDoesNotThrow(peripheral::teardown);
    }

}
