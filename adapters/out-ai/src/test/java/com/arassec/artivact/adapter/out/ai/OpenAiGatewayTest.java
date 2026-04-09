package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.domain.exception.ArtivactException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OpenAiGateway}.
 */
@ExtendWith(MockitoExtension.class)
class OpenAiGatewayTest {

    @Mock
    private OpenAiChatModel openAiChatModel;

    @Mock
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @InjectMocks
    private OpenAiGateway openAiGateway;

    /**
     * Tests that the gateway delegates translation to the OpenAI chat model.
     */
    @Test
    void testTranslate() {
        when(openAiChatModel.call(anyString())).thenReturn("Hallo");

        String result = openAiGateway.translate("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
        verify(openAiChatModel).call(anyString());
    }

    /**
     * Tests that the gateway converts text to audio and saves the file.
     */
    @Test
    void testConvertToAudio(@TempDir Path tempDir) {
        byte[] audioBytes = new byte[]{1, 2, 3, 4, 5};
        when(openAiAudioSpeechModel.call("Hello World")).thenReturn(audioBytes);

        Path targetFile = tempDir.resolve("test-audio.mp3");
        openAiGateway.convertToAudio("prompt", "Hello World", targetFile);

        assertThat(targetFile).exists();
        assertThat(targetFile).hasBinaryContent(audioBytes);
        verify(openAiAudioSpeechModel).call("Hello World");
    }

    /**
     * Tests that an exception is thrown when the audio file cannot be saved.
     */
    @Test
    void testConvertToAudioFileWriteError() {
        byte[] audioBytes = new byte[]{1, 2, 3};
        when(openAiAudioSpeechModel.call("content")).thenReturn(audioBytes);

        Path invalidPath = Path.of("/nonexistent/directory/audio.mp3");

        assertThatThrownBy(() -> openAiGateway.convertToAudio("prompt", "content", invalidPath))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not save audio file");
    }

}
