package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the AI adapter module.
 * <p>
 * Manually creates the {@link OpenAiChatModel} bean using the API key
 * from the {@link LoadAiConfigurationUseCase}.
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

}
