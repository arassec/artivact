package com.arassec.artivact.adapter.out.model.editor;

import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationModelSet;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests the {@link FallbackModelEditorPeripheral}.
 */
class FallbackModelEditorPeripheralTest {

    /**
     * The peripheral adapter under test.
     */
    private final FallbackModelEditorPeripheral peripheral = new FallbackModelEditorPeripheral();

    /**
     * Tests getting the peripheral implementation.
     */
    @Test
    void testGetSupportedImplementation() {
        assertThat(peripheral.getSupportedImplementation()).isEqualTo(PeripheralImplementation.FALLBACK_MODEL_EDITOR_PERIPHERAL);
    }

    /**
     * Tests opening a model with the fallback peripheral.
     */
    @Test
    void testOpen() {
        assertDoesNotThrow(() -> peripheral.open(CreationModelSet.builder().build()));
    }

}
