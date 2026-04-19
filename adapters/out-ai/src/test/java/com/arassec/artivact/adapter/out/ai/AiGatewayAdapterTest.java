package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.adapter.out.ai.gateway.AiGatewayAdapter;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;

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


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void executeReturnsNullIfApiKeyIsNotConfigured(String apiKey) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationApiKey(apiKey);

        String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsAiResponseIfOpenAiTranslationIsConfigured() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(AiModel.OPEN_AI);
        aiConfiguration.setTranslationApiKey("api-key");

        try (MockedConstruction<OpenAiChatModel> openAiChatModelMockedConstruction = Mockito.mockConstruction(
                OpenAiChatModel.class,
                (mock, ignored) -> when(mock.call("test prompt")).thenReturn("translated text"))) {

            String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

            assertThat(result).isEqualTo("translated text");
            assertThat(openAiChatModelMockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void executeDefaultsToOpenAiIfTranslationModelIsNull() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(null);
        aiConfiguration.setTranslationApiKey(null);

        String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeDefaultsToOpenAiAndReturnsResponseIfTranslationModelIsNull() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(null);
        aiConfiguration.setTranslationApiKey("api-key");

        try (MockedConstruction<OpenAiChatModel> openAiChatModelMockedConstruction = Mockito.mockConstruction(
                OpenAiChatModel.class,
                (mock, ignored) -> when(mock.call("test prompt")).thenReturn("translated text"))) {

            String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

            assertThat(result).isEqualTo("translated text");
            assertThat(openAiChatModelMockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void executeThrowsExceptionForUnsupportedTranslationModel() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationModel(AiModel.ELEVENLABS);

        assertThatThrownBy(() -> aiGatewayAdapter.execute(aiConfiguration, "test prompt"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Unsupported translation model");
    }

    @Test
    void convertToAudioThrowsExceptionIfOpenAiApiKeyMissing(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.OPEN_AI);
        aiConfiguration.setTtsApiKey(null);

        Path targetFile = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No API key or voice specified for TTS with OpenAI!");
    }

    @Test
    void convertToAudioThrowsExceptionIfOpenAiApiKeyIsEmpty(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.OPEN_AI);
        aiConfiguration.setTtsApiKey("");

        Path targetFile = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No API key or voice specified for TTS with OpenAI!");
    }

    @Test
    void convertToAudioThrowsExceptionIfElevenlabsApiKeyMissing(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.ELEVENLABS);
        aiConfiguration.setTtsApiKey(null);

        Path targetFile = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "voice", targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No API key or voice specified for TTS with Elevenlabs!");
    }

    @Test
    void convertToAudioThrowsExceptionIfElevenlabsApiKeyIsEmpty(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.ELEVENLABS);
        aiConfiguration.setTtsApiKey("   ");

        Path targetFile = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "voice", targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No API key or voice specified for TTS with Elevenlabs!");
    }

    @Test
    void convertToAudioWritesOpenAiAudioToTargetFile(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.OPEN_AI);
        aiConfiguration.setTtsApiKey("api-key");

        Path targetFile = tempDir.resolve("output.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        try (MockedConstruction<OpenAiAudioSpeechModel> openAiAudioSpeechModelMockedConstruction = Mockito.mockConstruction(
                OpenAiAudioSpeechModel.class,
                (mock, ignored) -> when(mock.call("content")).thenReturn(audioBytes))) {

            aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile);

            verify(fileRepository).write(targetFile, audioBytes);
            assertThat(openAiAudioSpeechModelMockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void convertToAudioDefaultsToOpenAiAndWritesAudioToTargetFile(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(null);
        aiConfiguration.setTtsApiKey("api-key");

        Path targetFile = tempDir.resolve("output.mp3");
        byte[] audioBytes = new byte[]{1, 2, 3};

        try (MockedConstruction<OpenAiAudioSpeechModel> openAiAudioSpeechModelMockedConstruction = Mockito.mockConstruction(
                OpenAiAudioSpeechModel.class,
                (mock, ignored) -> when(mock.call("content")).thenReturn(audioBytes))) {

            aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile);

            verify(fileRepository).write(targetFile, audioBytes);
            assertThat(openAiAudioSpeechModelMockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void convertToAudioWritesElevenlabsAudioToTargetFile(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.ELEVENLABS);
        aiConfiguration.setTtsApiKey("api-key");

        Path targetFile = tempDir.resolve("output.mp3");
        byte[] audioBytes = new byte[]{4, 5, 6};

        try (MockedConstruction<ElevenLabsTextToSpeechModel> elevenLabsTextToSpeechModelMockedConstruction = Mockito.mockConstruction(
                ElevenLabsTextToSpeechModel.class,
                (mock, ignored) -> when(mock.call("content")).thenReturn(audioBytes))) {

            aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "voice", targetFile);

            verify(fileRepository).write(targetFile, audioBytes);
            assertThat(elevenLabsTextToSpeechModelMockedConstruction.constructed()).hasSize(1);
        }
    }

    @Test
    void convertToAudioDoesNotWriteFileIfNoAudioWasGenerated(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(AiModel.OPEN_AI);
        aiConfiguration.setTtsApiKey("api-key");

        Path targetFile = tempDir.resolve("output.mp3");

        try (var _ = Mockito.mockConstruction(
                OpenAiAudioSpeechModel.class,
                (mock, ignoredContext) -> when(mock.call("content")).thenReturn(new byte[0]))) {

            aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile);

            verify(fileRepository, never()).write(any(Path.class), any(byte[].class));
        }
    }

}
