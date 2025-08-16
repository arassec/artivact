package com.arassec.artivact.adapter.out.os;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests the {@link OperatingSystemAdapter}.
 */
class OperatingSystemAdapterTest {

    /**
     * Tests executing a command on the command line.
     */
    @Test
    void testExecute() {
        OperatingSystemAdapter operatingSystemAdapter = new OperatingSystemAdapter();
        assertDoesNotThrow(() -> operatingSystemAdapter.execute("echo", List.of("test")));
    }

}
