package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.adapter.out.ai.gateway.OpenAiGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link OpenAiGateway}.
 */
@ExtendWith(MockitoExtension.class)
class OpenAiGatewayTest {

    private final OpenAiGateway openAiGateway = new OpenAiGateway();

    @Test
    void executeReturnsNullWhenAiIsDisabled() {
        AiConfiguration aiConfiguration = aiConfiguration(false, "api-key");

        String result = openAiGateway.execute(aiConfiguration, "prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsNullWhenApiKeyIsBlank() {
        AiConfiguration aiConfiguration = aiConfiguration(true, " ");

        String result = openAiGateway.execute(aiConfiguration, "prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsAiResponseWhenConfigurationIsValid() {
        AiConfiguration aiConfiguration = aiConfiguration(true, "api-key");
        OpenAiChatModel.Builder builder = mock(OpenAiChatModel.Builder.class);
        OpenAiChatModel chatModel = mock(OpenAiChatModel.class);

        try (MockedStatic<OpenAiChatModel> openAiChatModelMock = org.mockito.Mockito.mockStatic(OpenAiChatModel.class)) {
            openAiChatModelMock.when(OpenAiChatModel::builder).thenReturn(builder);
            when(builder.openAiApi(any(OpenAiApi.class))).thenReturn(builder);
            when(builder.build()).thenReturn(chatModel);
            when(chatModel.call("prompt")).thenReturn("response");

            String result = openAiGateway.execute(aiConfiguration, "prompt");

            assertThat(result).isEqualTo("response");
            verify(chatModel).call("prompt");
        }
    }

    @Test
    void convertToAudioDoesNothingWhenAiIsDisabled(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = aiConfiguration(false, "api-key");
        Path targetFile = tempDir.resolve("audio.mp3");

        openAiGateway.convertToAudio(aiConfiguration, "prompt", targetFile);

        assertThat(targetFile).doesNotExist();
    }

    @Test
    void convertToAudioDoesNothingWhenApiKeyIsBlank(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = aiConfiguration(true, " ");
        Path targetFile = tempDir.resolve("audio.mp3");

        openAiGateway.convertToAudio(aiConfiguration, "prompt", targetFile);

        assertThat(targetFile).doesNotExist();
    }

    @Test
    void convertToAudioWritesGeneratedAudioToTargetFile(@TempDir Path tempDir) throws Exception {
        AiConfiguration aiConfiguration = aiConfiguration(true, "api-key");
        Path targetFile = tempDir.resolve("audio.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        try (MockedConstruction<OpenAiAudioSpeechModel> ignored =
                     org.mockito.Mockito.mockConstruction(OpenAiAudioSpeechModel.class,
                             (mock, context) -> when(mock.call("prompt")).thenReturn(audioBytes))) {
            openAiGateway.convertToAudio(aiConfiguration, "prompt", targetFile);
        }

        assertThat(Files.readAllBytes(targetFile)).isEqualTo(audioBytes);
    }

    @Test
    void convertToAudioThrowsArtivactExceptionWhenTargetFileCannotBeWritten(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = aiConfiguration(true, "api-key");
        Path targetFile = tempDir.resolve("missing-directory").resolve("audio.mp3");

        try (MockedConstruction<OpenAiAudioSpeechModel> ignored =
                     org.mockito.Mockito.mockConstruction(OpenAiAudioSpeechModel.class,
                             (mock, context) -> when(mock.call("prompt")).thenReturn(new byte[]{1, 2, 3}))) {
            assertThatThrownBy(() -> openAiGateway.convertToAudio(aiConfiguration, "prompt", targetFile))
                    .isInstanceOf(ArtivactException.class)
                    .hasMessageContaining(targetFile.toString());
        }
    }

    private AiConfiguration aiConfiguration(boolean enabled, String apiKey) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setEnabled(enabled);
        aiConfiguration.setApiKey(apiKey);
        aiConfiguration.setTtsVoice("alloy");
        return aiConfiguration;
    }

}