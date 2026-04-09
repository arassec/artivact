package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration class for the AI adapter module.
 * <p>
 * Manually creates the {@link OpenAiChatModel} and {@link OpenAiAudioSpeechModel} beans
 * using the API key from the {@link LoadAiConfigurationUseCase}.
 */
@Configuration
public class AiAdapterConfiguration {

    /**
     * Creates the {@link OpenAiChatModel} bean with the API key obtained
     * from the application's AI configuration.
     *
     * @param loadAiConfigurationUseCase The use case to load the AI configuration.
     * @return A configured {@link OpenAiChatModel} instance.
     */
    @Bean
    public OpenAiChatModel openAiChatModel(LoadAiConfigurationUseCase loadAiConfigurationUseCase) {
        AiConfiguration aiConfiguration = loadAiConfigurationUseCase.loadAiConfiguration();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(aiConfiguration.getApiKey())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();
    }

    /**
     * Creates the {@link OpenAiAudioSpeechModel} bean with the API key and voice
     * obtained from the application's AI configuration.
     *
     * @param loadAiConfigurationUseCase The use case to load the AI configuration.
     * @return A configured {@link OpenAiAudioSpeechModel} instance.
     */
    @Bean
    public OpenAiAudioSpeechModel openAiAudioSpeechModel(LoadAiConfigurationUseCase loadAiConfigurationUseCase) {
        AiConfiguration aiConfiguration = loadAiConfigurationUseCase.loadAiConfiguration();

        OpenAiAudioApi openAiAudioApi = OpenAiAudioApi.builder()
                .apiKey(aiConfiguration.getApiKey())
                .build();

        String voice = StringUtils.hasText(aiConfiguration.getTtsVoice())
                ? aiConfiguration.getTtsVoice()
                : "alloy";

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(voice)
                .responseFormat(AudioResponseFormat.MP3)
                .build();

        return new OpenAiAudioSpeechModel(openAiAudioApi, options);
    }

}
