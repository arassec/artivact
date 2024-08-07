package com.arassec.artivact.web.security;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the {@link ArtivactDesktopSecurityConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
class ArtivactDesktopSecurityConfigurationTest {

    /**
     * The tested configuration.
     */
    private final ArtivactDesktopSecurityConfiguration desktopSecurityConfiguration = new ArtivactDesktopSecurityConfiguration();

    /**
     * Mocked {@link HttpSecurity}.
     */
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpSecurity httpSecurity;

    /**
     * Tests the filter chain configuration.
     */
    @Test
    @SneakyThrows
    void testFilterChain() {
        assertNotNull(desktopSecurityConfiguration.filterChain(httpSecurity));
    }

}
