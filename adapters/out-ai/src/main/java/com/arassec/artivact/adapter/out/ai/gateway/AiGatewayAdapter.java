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
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
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
                ? AiModel.OpenAI
                : aiConfiguration.getTranslationModel();

        if (translationModel != AiModel.OpenAI) {
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
    public void convertToAudio(AiConfiguration aiConfiguration, String prompt, String voice, Path targetFile) {
        AiModel ttsModel = aiConfiguration.getTtsModel() == null
                ? AiModel.OpenAI
                : aiConfiguration.getTtsModel();

        byte[] audioBytes = switch (ttsModel) {
            case OpenAI -> convertToAudioWithOpenAi(aiConfiguration, prompt, voice);
            case Elevenlabs -> convertToAudioWithElevenlabs(aiConfiguration, prompt, voice);
        };

        if (audioBytes == null || audioBytes.length == 0) {
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
     * @param aiConfiguration The AI configuration.
     * @param voice           The configured voice.
     * @return A configured {@link OpenAiAudioSpeechModel} instance.
     */
    private TextToSpeechModel openAiAudioSpeechModel(AiConfiguration aiConfiguration, String voice) {

        if (!StringUtils.hasText(aiConfiguration.getTtsApiKey())) {
            return null;
        }

        OpenAiAudioApi openAiAudioApi = OpenAiAudioApi.builder()
                .apiKey(aiConfiguration.getTtsApiKey())
                .build();

        String resolvedVoice = StringUtils.hasText(voice)
                ? voice
                : AiConfiguration.DEFAULT_TTS_VOICE;

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(resolvedVoice)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OPEN_AI_TTS_MODEL)
                .build();

        return new OpenAiAudioSpeechModel(openAiAudioApi, options);
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
        TextToSpeechModel textToSpeechModel = openAiAudioSpeechModel(aiConfiguration, voice);

        if (textToSpeechModel == null) {
            return null;
        }

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
            return null;
        }

        try {
            log.debug("Converting content to audio via Elevenlabs TTS.");

            String requestBody = createJsonMapper().writeValueAsString(Map.of(
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

            HttpResponse<byte[]> response = createHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());

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

    /**
     * Creates an HTTP client.
     *
     * @return The HTTP client.
     */
    public HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * Creates the JSON mapper used for HTTP payloads.
     *
     * @return The JSON mapper.
     */
    public JsonMapper createJsonMapper() {
        return JsonMapper.builder().build();
    }

}
