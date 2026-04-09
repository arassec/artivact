package com.arassec.artivact.application.service.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link AiService}.
 */
class AiServiceTest {

    private final AiService aiService = new AiService();

    /**
     * Tests that the stub implementation returns the input text unchanged.
     */
    @Test
    void testTranslateText() {
        String result = aiService.translateText("Hello", "de");
        assertThat(result).isEqualTo("Hello");
    }

}
