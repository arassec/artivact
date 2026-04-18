package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.adapter.out.ai.gateway.AiGatewayAdapter;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
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
    void executeReturnsNullWhenApiKeyIsBlank() {
        AiConfiguration aiConfiguration = translationConfiguration(" ");

        String result = aiGatewayAdapter.execute(aiConfiguration, "prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsAiResponseWhenConfigurationIsValid() {
        AiConfiguration aiConfiguration = translationConfiguration("api-key");
        OpenAiChatModel.Builder builder = mock(OpenAiChatModel.Builder.class);
        OpenAiChatModel chatModel = mock(OpenAiChatModel.class);

        try (MockedStatic<OpenAiChatModel> openAiChatModelMock = org.mockito.Mockito.mockStatic(OpenAiChatModel.class)) {
            openAiChatModelMock.when(OpenAiChatModel::builder).thenReturn(builder);
            when(builder.openAiApi(any(OpenAiApi.class))).thenReturn(builder);
            when(builder.build()).thenReturn(chatModel);
            when(chatModel.call("prompt")).thenReturn("response");

            String result = aiGatewayAdapter.execute(aiConfiguration, "prompt");

            assertThat(result).isEqualTo("response");
            verify(chatModel).call("prompt");
        }
    }

    @Test
    void convertToAudioDoesNothingWhenApiKeyIsBlank(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.OpenAI, " ");
        Path targetFile = tempDir.resolve("audio.mp3");

        aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "alloy", targetFile);

        assertThat(targetFile).doesNotExist();
    }

    @SuppressWarnings("unused")
    @Test
    void convertToAudioWritesGeneratedOpenAiAudioToTargetFile(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.OpenAI, "api-key");
        Path targetFile = tempDir.resolve("audio.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        try (var ignored =
                     org.mockito.Mockito.mockConstruction(OpenAiAudioSpeechModel.class,
                              (mock, context) -> when(mock.call("prompt")).thenReturn(audioBytes))) {
            aiGatewayAdapter.convertToAudio(aiConfiguration, "prompt", "alloy", targetFile);
        }

        verify(fileRepository, times(1)).write(targetFile, audioBytes);
    }

    @SuppressWarnings("unchecked")
    @Test
    void convertToAudioWritesGeneratedElevenlabsAudioToTargetFile(@TempDir Path tempDir) throws Exception {
        AiConfiguration aiConfiguration = ttsConfiguration(AiModel.Elevenlabs, "api-key");
        Path targetFile = tempDir.resolve("audio.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<byte[]> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(audioBytes);
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        AiGatewayAdapter gatewaySpy = spy(aiGatewayAdapter);
        doReturn(httpClient).when(gatewaySpy).createHttpClient();
        doReturn(JsonMapper.builder().build()).when(gatewaySpy).createJsonMapper();

        gatewaySpy.convertToAudio(aiConfiguration, "prompt", "voice-id", targetFile);

        verify(fileRepository).write(targetFile, audioBytes);
    }

    @Test
    void executeRejectsUnsupportedTranslationModel() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(AiModel.Elevenlabs);
        aiConfiguration.setTranslationApiKey("api-key");

        assertThatThrownBy(() -> aiGatewayAdapter.execute(aiConfiguration, "prompt"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Unsupported translation model: Elevenlabs");
    }

    private AiConfiguration translationConfiguration(String apiKey) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(AiModel.OpenAI);
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
