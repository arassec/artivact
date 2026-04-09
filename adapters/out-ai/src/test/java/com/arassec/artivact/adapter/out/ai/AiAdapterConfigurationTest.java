package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AiAdapterConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
class AiAdapterConfigurationTest {

    @Mock
    private LoadAiConfigurationUseCase loadAiConfigurationUseCase;

    @InjectMocks
    private AiAdapterConfiguration aiAdapterConfiguration;

    /**
     * Tests that the configuration creates an OpenAiChatModel bean using the API key from the AI configuration.
     */
    @Test
    void testOpenAiChatModelBeanCreation() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setApiKey("test-api-key");

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        OpenAiChatModel result = aiAdapterConfiguration.openAiChatModel(loadAiConfigurationUseCase);

        assertThat(result).isNotNull();
    }

    /**
     * Tests that the configuration creates an OpenAiAudioSpeechModel bean using the API key and voice.
     */
    @Test
    void testOpenAiAudioSpeechModelBeanCreation() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setApiKey("test-api-key");
        aiConfiguration.setTtsVoice("alloy");

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        OpenAiAudioSpeechModel result = aiAdapterConfiguration.openAiAudioSpeechModel(loadAiConfigurationUseCase);

        assertThat(result).isNotNull();
    }

    /**
     * Tests that the configuration uses the default voice when no voice is configured.
     */
    @Test
    void testOpenAiAudioSpeechModelDefaultVoice() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setApiKey("test-api-key");
        aiConfiguration.setTtsVoice("");

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

        OpenAiAudioSpeechModel result = aiAdapterConfiguration.openAiAudioSpeechModel(loadAiConfigurationUseCase);

        assertThat(result).isNotNull();
    }

}
