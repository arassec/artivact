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
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

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


        if (!StringUtils.hasText(aiConfiguration.getTranslationApiKey())) {
            throw new ArtivactException("Missing API key for translation model!");
        }

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(aiConfiguration.getTranslationApiKey())
                .build();

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();

        log.debug("Calling the AI with the following prompt: '{}'", prompt);

        return chatModel.call(prompt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void convertToAudio(AiConfiguration aiConfiguration, String prompt, String voice, Path targetFile) {
        AiModel ttsModel = aiConfiguration.getTtsModel() == null
                ? AiModel.OPEN_AI
                : aiConfiguration.getTtsModel();

        byte[] audioBytes = switch (ttsModel) {
            case OPEN_AI -> convertToAudioWithOpenAi(aiConfiguration, prompt, voice);
            case ELEVENLABS -> convertToAudioWithElevenlabs(aiConfiguration, prompt, voice);
        };

        if (audioBytes == null || audioBytes.length == 0) {
            return;
        }

        fileRepository.write(targetFile, audioBytes);
    }

    /**
     * Converts the given content to audio using OpenAI.
     *
     * @param aiConfiguration The AI configuration.
     * @param prompt          The content to convert.
     * @param voice           The configured voice.
     * @return The generated audio data.
     */
    private byte[] convertToAudioWithOpenAi(AiConfiguration aiConfiguration, String prompt, String voice) {
        if (!StringUtils.hasText(aiConfiguration.getTtsApiKey()) || !StringUtils.hasText(voice)) {
            throw new ArtivactException("Missing API key or voice for TTS model!");
        }

        OpenAiAudioApi openAiAudioApi = OpenAiAudioApi.builder()
                .apiKey(aiConfiguration.getTtsApiKey())
                .build();

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(voice)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OPEN_AI_TTS_MODEL)
                .build();

        TextToSpeechModel textToSpeechModel = new OpenAiAudioSpeechModel(openAiAudioApi, options);

        log.debug("Converting content to audio via OpenAI TTS.");

        return textToSpeechModel.call(prompt);
    }

    /**
     * Converts the given content to audio using Elevenlabs.
     *
     * @param aiConfiguration The AI configuration.
     * @param prompt          The content to convert.
     * @param voice           The configured voice ID.
     * @return The generated audio data.
     */
    byte[] convertToAudioWithElevenlabs(AiConfiguration aiConfiguration, String prompt, String voice) {
        if (!StringUtils.hasText(aiConfiguration.getTtsApiKey()) || !StringUtils.hasText(voice)) {
            throw new ArtivactException("Missing API key or voice for TTS model!");
        }

        try (var httpClient = HttpClient.newHttpClient()) {
            log.debug("Converting content to audio via Elevenlabs TTS.");

            String requestBody = JsonMapper.builder().build().writeValueAsString(Map.of(
                    "text", prompt,
                    "model_id", ELEVENLABS_TTS_MODEL
            ));

            String encodedVoice = URLEncoder.encode(voice, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + encodedVoice + "?output_format=mp3_44100_128"))
                    .header("xi-api-key", aiConfiguration.getTtsApiKey())
                    .header("Content-Type", "application/json")
                    .header("accept", "audio/mpeg")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() >= 400) {
                throw new ArtivactException("Elevenlabs TTS request failed with status: " + response.statusCode());
            }

            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactException("Elevenlabs TTS request interrupted!", e);
        } catch (IOException e) {
            throw new ArtivactException("Elevenlabs TTS request failed!", e);
        }
    }

}
