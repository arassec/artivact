package com.arassec.artivact.adapter.out.ai.gateway;

import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.elevenlabs.api.ElevenLabsApi;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

/**
 * Implements the {@link AiGateway} port using supported AI providers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiGatewayAdapter implements AiGateway {

    /**
     * The default OpenAI speech model.
     */
    private static final String OPEN_AI_TTS_MODEL = "gpt-4o-mini-tts";

    /**
     * The default Elevenlabs speech model.
     */
    private static final String ELEVENLABS_TTS_MODEL = "eleven_multilingual_v2";

    /**
     * The application's file repository.
     */
    private final FileRepository fileRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(AiConfiguration aiConfiguration, String prompt) {
        AiModel translationModel = aiConfiguration.getTranslationModel() == null
                ? AiModel.OPEN_AI
                : aiConfiguration.getTranslationModel();

        if (translationModel != AiModel.OPEN_AI) {
            throw new ArtivactException("Unsupported translation model: " + translationModel);
        }

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
    @Override
    public void convertToAudio(AiConfiguration aiConfiguration, String content, String voice, Path targetFile) {
        AiModel ttsModel = aiConfiguration.getTtsModel() == null
                ? AiModel.OPEN_AI
                : aiConfiguration.getTtsModel();

        TextToSpeechModel textToSpeechModel = switch (ttsModel) {
            case OPEN_AI -> openAiAudioSpeechModel(aiConfiguration.getTtsApiKey(), voice);
            case ELEVENLABS -> elevenlabsSpeechModel(aiConfiguration.getTtsApiKey(), voice);
        };

        byte[] audioBytes = textToSpeechModel.call(content);

        if (audioBytes.length == 0) {
            return;
        }

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

        if (!StringUtils.hasText(aiConfiguration.getTranslationApiKey())) {
            return null;
        }

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(aiConfiguration.getTranslationApiKey())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();
    }

    /**
     * Creates the {@link OpenAiAudioSpeechModel} bean with the API key and voice
     * obtained from the application's AI configuration.
     *
     * @param apiKey The API Key.
     * @param voice  The configured voice.
     * @return A configured {@link OpenAiAudioSpeechModel} instance.
     */
    private TextToSpeechModel openAiAudioSpeechModel(String apiKey, String voice) {

        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(voice)) {
            throw new ArtivactException("No API key or voice specified for TTS with OpenAI!");
        }

        OpenAiAudioApi openAiAudioApi = OpenAiAudioApi.builder()
                .apiKey(apiKey)
                .build();

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(voice)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OPEN_AI_TTS_MODEL)
                .build();

        return new OpenAiAudioSpeechModel(openAiAudioApi, options);
    }

    /**
     * Creates the {@link ElevenLabsTextToSpeechModel} bean with the API key and voice
     * obtained from the application's AI configuration.
     *
     * @param apiKey The API Key.
     * @param voice  The configured voice.
     * @return A configured {@link ElevenLabsTextToSpeechModel} instance.
     */
    private TextToSpeechModel elevenlabsSpeechModel(String apiKey, String voice) {

        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(voice)) {
            throw new ArtivactException("No API key or voice specified for TTS with Elevenlabs!");
        }

        ElevenLabsApi elevenLabsApi = ElevenLabsApi.builder()
                .apiKey(apiKey)
                .build();

        ElevenLabsTextToSpeechOptions options = ElevenLabsTextToSpeechOptions.builder()
                .voice(voice)
                .model(ELEVENLABS_TTS_MODEL)
                .build();

        return new ElevenLabsTextToSpeechModel(elevenLabsApi, options);
    }

}
