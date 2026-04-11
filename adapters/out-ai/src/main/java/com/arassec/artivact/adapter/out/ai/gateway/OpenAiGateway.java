package com.arassec.artivact.adapter.out.ai.gateway;

import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

/**
 * Implements the {@link AiGateway} port using OpenAI via Spring AI.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiGateway implements AiGateway {

    /**
     * The application's file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(AiConfiguration aiConfiguration, String prompt) {
        ChatModel chatModel = openAiChatModel(aiConfiguration);

        if (chatModel == null) {
            return null;
        }

        log.debug("Calling the AI with the following prompt: '{}'", prompt);

        return chatModel.call(prompt);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("javasecurity:S2083")
    // The file path is controlled and validated by the application, not user input.
    @Override
    public void convertToAudio(AiConfiguration aiConfiguration, String prompt, Path targetFile) {
        TextToSpeechModel textToSpeechModel = openAiAudioSpeechModel(aiConfiguration);

        if (textToSpeechModel == null) {
            return;
        }

        log.debug("Converting content to audio via OpenAI TTS.");

        byte[] audioBytes = textToSpeechModel.call(prompt);

        fileRepository.write(targetFile, audioBytes);
    }


    /**
     * Creates the {@link OpenAiChatModel} bean with the API key obtained
     * from the application's AI configuration.
     *
     * @param aiConfiguration The AI configuration.
     * @return A configured {@link OpenAiChatModel} instance.
     */
    private ChatModel openAiChatModel(AiConfiguration aiConfiguration) {

        if (!aiConfiguration.isEnabled() || !StringUtils.hasText(aiConfiguration.getApiKey())) {
            return null;
        }

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
     * @param aiConfiguration The AI configuration.
     * @return A configured {@link OpenAiAudioSpeechModel} instance.
     */
    private TextToSpeechModel openAiAudioSpeechModel(AiConfiguration aiConfiguration) {

        if (!aiConfiguration.isEnabled() || !StringUtils.hasText(aiConfiguration.getApiKey())) {
            return null;
        }

        OpenAiAudioApi openAiAudioApi = OpenAiAudioApi.builder()
                .apiKey(aiConfiguration.getApiKey())
                .build();

        String voice = StringUtils.hasText(aiConfiguration.getTtsVoice())
                ? aiConfiguration.getTtsVoice()
                : "alloy";

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(voice)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model("gpt-4o-mini-tts")
                .build();

        return new OpenAiAudioSpeechModel(openAiAudioApi, options);
    }

}
