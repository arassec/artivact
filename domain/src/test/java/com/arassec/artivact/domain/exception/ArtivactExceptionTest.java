package com.arassec.artivact.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests instantiation of {@link ArtivactException}s.
 */
class ArtivactExceptionTest {

    /**
     * Tests the constructor with message parameter.
     */
    @Test
    void testInstantiationWithMessage() {
        ArtivactException artivactException = new ArtivactException("test");
        assertThat(artivactException.getMessage()).isEqualTo("test");
    }

    /**
     * Tests the constructor with message and cause parameter.
     */
    @Test
    void testInstantiationWithMessageAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("test");
        ArtivactException artivactException = new ArtivactException("test", cause);

        assertThat(artivactException.getMessage()).isEqualTo("test");
        assertThat(artivactException.getCause()).isEqualTo(cause);
    }

}
