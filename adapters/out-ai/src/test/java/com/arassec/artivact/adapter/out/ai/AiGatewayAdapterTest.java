package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.adapter.out.ai.gateway.AiGatewayAdapter;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link AiGatewayAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class AiGatewayAdapterTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private AiGatewayAdapter aiGatewayAdapter;

    @Test
    void executeReturnsChatModelResponseWhenTranslationModelIsNotConfigured() {
        AiConfiguration aiConfiguration = translationConfiguration(null, "api-key");

        try (MockedConstruction<OpenAiChatModel> mockedConstruction = Mockito.mockConstruction(
                OpenAiChatModel.class,
                (mock, _) -> when(mock.call("prompt")).thenReturn("response")
        )) {
            String result = aiGatewayAdapter.execute(aiConfiguration, "prompt");

            assertThat(result).isEqualTo("response");
            assertThat(mockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void executeThrowsExceptionWhenTranslationModelIsUnsupported() {
        AiConfiguration aiConfiguration = translationConfiguration(AiModel.ELEVENLABS, "api-key");

        assertThatThrownBy(() -> aiGatewayAdapter.execute(aiConfiguration, "prompt"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Unsupported translation model: ELEVENLABS");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void executeThrowsExceptionWhenTranslationApiKeyIsMissing(String apiKey) {
        AiConfiguration aiConfiguration = translationConfiguration(AiModel.OPEN_AI, apiKey);

        assertThatThrownBy(() -> aiGatewayAdapter.execute(aiConfiguration, "prompt"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Missing API key for translation model!");
    }

    @Test
    void convertToAudioWritesOpenAiAudioToTargetFileWhenTtsModelIsNotConfigured() {
        AiConfiguration aiConfiguration = ttsConfiguration(null, "api-key");
        Path targetFile = Path.of("audio.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        try (MockedConstruction<OpenAiAudioSpeechModel> mockedConstruction = Mockito.mockConstruction(
                OpenAiAudioSpeechModel.class,
                (mock, _) -> when(mock.call("prompt")).thenReturn(audioBytes)
        )) {
            aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "alloy", targetFile);

            verify(fileRepository).write(targetFile, audioBytes);
            assertThat(mockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void convertToAudioDoesNotWriteFileWhenOpenAiReturnsNoAudio() {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.OPEN_AI, "api-key");
        Path targetFile = Path.of("audio.mp3");

        try (MockedConstruction<OpenAiAudioSpeechModel> mockedConstruction = Mockito.mockConstruction(
                OpenAiAudioSpeechModel.class,
                (mock, _) -> when(mock.call("prompt")).thenReturn(new byte[0])
        )) {
            aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "alloy", targetFile);

            verify(fileRepository, never()).write(any(Path.class), any(byte[].class));
            assertThat(mockedConstruction.constructed()).hasSize(1);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void convertToAudioThrowsExceptionWhenOpenAiApiKeyIsMissing(String apiKey) {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.OPEN_AI, apiKey);

        var audioMp3 = Path.of("audio.mp3");
        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "alloy", audioMp3))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Missing API key or voice for TTS model!");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void convertToAudioThrowsExceptionWhenOpenAiVoiceIsMissing(String voice) {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.OPEN_AI, "api-key");

        var audioMp3 = Path.of("audio.mp3");
        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", voice, audioMp3))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Missing API key or voice for TTS model!");
    }

    @Test
    void convertToAudioWritesElevenlabsAudioToTargetFile() throws Exception {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.ELEVENLABS, "api-key");
        Path targetFile = Path.of("audio.mp3");
        byte[] audioBytes = new byte[]{4, 5, 6};

        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(audioBytes);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "voice-id", targetFile);

            verify(fileRepository).write(targetFile, audioBytes);
        }
    }

    @Test
    void convertToAudioThrowsExceptionWhenElevenlabsReturnsErrorStatus() throws Exception {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.ELEVENLABS, "api-key");

        HttpClient httpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenReturn(response);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            var audioMp3 = Path.of("audio.mp3");
            assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(
                    aiConfiguration,
                    "prompt",
                    "voice-id",
                    audioMp3
            ))
                    .isInstanceOf(ArtivactException.class)
                    .hasMessage("Elevenlabs TTS request failed with status: 500");
        }
    }

    @Test
    void convertToAudioThrowsExceptionWhenElevenlabsRequestFails() throws Exception {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.ELEVENLABS, "api-key");

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenThrow(new IOException("boom"));

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);

            var audioMp3 = Path.of("audio.mp3");
            assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(
                    aiConfiguration,
                    "prompt",
                    "voice-id",
                    audioMp3
            ))
                    .isInstanceOf(ArtivactException.class)
                    .hasMessage("Elevenlabs TTS request failed!");
        }
    }

    private AiConfiguration translationConfiguration(AiModel translationModel, String apiKey) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(translationModel);
        aiConfiguration.setTranslationApiKey(apiKey);
        return aiConfiguration;
    }

    private AiConfiguration ttsConfiguration(AiModel ttsModel, String apiKey) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(ttsModel);
        aiConfiguration.setTtsApiKey(apiKey);
        return aiConfiguration;
    }

}