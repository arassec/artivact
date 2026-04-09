package com.arassec.artivact.adapter.out.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OpenAiGateway}.
 */
@ExtendWith(MockitoExtension.class)
class OpenAiGatewayTest {

    @Mock
    private OpenAiChatModel openAiChatModel;

    @InjectMocks
    private OpenAiGateway openAiGateway;

    /**
     * Tests that the gateway delegates translation to the OpenAI chat model.
     */
    @Test
    void testTranslate() {
        when(openAiChatModel.call(anyString())).thenReturn("Hallo");

        String result = openAiGateway.translate("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
        verify(openAiChatModel).call(anyString());
    }

}
