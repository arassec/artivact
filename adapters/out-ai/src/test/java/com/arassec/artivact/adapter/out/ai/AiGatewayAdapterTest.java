package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.adapter.out.ai.gateway.AiGatewayAdapter;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the {@link AiGatewayAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class AiGatewayAdapterTest {

    @InjectMocks
    private AiGatewayAdapter aiGatewayAdapter;

    @Test
    void executeReturnsNullIfNoApiKeyConfigured() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationApiKey(null);

        String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsNullIfApiKeyIsEmpty() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationApiKey("");

        String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

        assertThat(result).isNull();
    }

    @Test
    void executeReturnsNullIfApiKeyIsBlank() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationApiKey("   ");

        String result = aiGatewayAdapter.execute(aiConfiguration, "test prompt");

        assertThat(result).isNull();
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
    void convertToAudioDefaultsToOpenAiIfTtsModelIsNull(@TempDir Path tempDir) {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTtsModel(null);
        aiConfiguration.setTtsApiKey(null);

        Path targetFile = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> aiGatewayAdapter.convertToAudio(aiConfiguration, "content", "alloy", targetFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No API key or voice specified for TTS with OpenAI!");
    }

}
