package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests the {@link ArduinoTurntablePeripheral}.
 */
class ArduinoTurntablePeripheralTest {

    /**
     * Peripheral under test.
     */
    private final ArduinoTurntablePeripheral defaultTurntablePeripheral = new ArduinoTurntablePeripheral();

    /**
     * Tests getting the supported implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(defaultTurntablePeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.ARDUINO_TURNTABLE_PERIPHERAL);
    }

    /**
     * Tests rotating the turntable.
     * <p>
     * Real test will be implemented after final re-implementation with firmata4j!
     */
    @Test
    void testRotate() {
        assertDoesNotThrow(defaultTurntablePeripheral::teardown);
    }

}
