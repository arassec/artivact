package com.arassec.artivact.application.service.maintenance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests the {@link CleanupProjectFilesService}.
 */
@ExtendWith(MockitoExtension.class)
class CleanupProjectFilesServiceTest {

    @InjectMocks
    private CleanupProjectFilesService service;

    @Test
    void testCleanup() {
        assertThatNoException().isThrownBy(() -> service.cleanup());
    }

}
