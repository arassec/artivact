package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.domain.exception.ArtivactException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implements the {@link AiGateway} port using OpenAI via Spring AI.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiGateway implements AiGateway {

    /**
     * The OpenAI chat model for AI interactions.
     */
    private final OpenAiChatModel openAiChatModel;

    /**
     * The OpenAI audio speech model for text-to-speech.
     */
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    /**
     * {@inheritDoc}
     */
    @Override
    public String translate(String text, String targetLocale) {
        log.debug("Translating text to locale '{}' via OpenAI.", targetLocale);

        String prompt = "Translate the following text into the locale '" + targetLocale
                + "'. Only return the translated text, nothing else.\n\n" + text;

        return openAiChatModel.call(prompt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void convertToAudio(String prompt, String content, Path targetFile) {
        log.debug("Converting content to audio via OpenAI TTS.");

        byte[] audioBytes = openAiAudioSpeechModel.call(content);

        try {
            Files.write(targetFile, audioBytes);
        } catch (IOException e) {
            throw new ArtivactException("Could not save audio file: " + targetFile, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String chat(String prompt) {
        log.debug("Sending chat prompt to OpenAI.");
        return openAiChatModel.call(prompt);
    }

}
