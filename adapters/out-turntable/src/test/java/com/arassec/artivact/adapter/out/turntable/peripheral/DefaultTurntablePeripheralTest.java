package com.arassec.artivact.adapter.out.turntable.peripheral;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests the {@link DefaultTurntablePeripheral}.
 */
class DefaultTurntablePeripheralTest {

    /**
     * Peripheral under test.
     */
    private final DefaultTurntablePeripheral defaultTurntablePeripheral = new DefaultTurntablePeripheral();

    /**
     * Tests getting the supported implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(defaultTurntablePeripheral.getSupportedImplementation())
                .isEqualTo(PeripheralImplementation.DEFAULT_TURNTABLE_PERIPHERAL);
    }

    /**
     * Tests rotating the turntable.
     * <p>
     * TODO: Implement real test after re-implementation with firmata4j!
     */
    @Test
    void testRotate() {
        assertDoesNotThrow(() -> defaultTurntablePeripheral.initialize(null, null));
        assertDoesNotThrow(() -> defaultTurntablePeripheral.rotate(1, 100));
        assertDoesNotThrow(defaultTurntablePeripheral::teardown);
    }

}
