package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.out.gateway.AiGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AiService}.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiGateway aiGateway;

    @InjectMocks
    private AiService aiService;

    /**
     * Tests that the service delegates translation to the AI gateway.
     */
    @Test
    void testTranslateText() {
        when(aiGateway.translate("Hello", "de")).thenReturn("Hallo");

        String result = aiService.translateText("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
    }

}
