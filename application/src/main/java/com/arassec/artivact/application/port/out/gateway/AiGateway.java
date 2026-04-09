package com.arassec.artivact.application.port.out.gateway;

import java.nio.file.Path;

/**
 * Gateway for AI-based operations.
 */
public interface AiGateway {

    /**
     * Translates the given text into the specified target locale.
     *
     * @param text         The text to translate.
     * @param targetLocale The target locale for the translation.
     * @return The translated text.
     */
    String translate(String text, String targetLocale);

    /**
     * Converts the given content to an audio file using text-to-speech.
     *
     * @param prompt     The prompt to guide the TTS generation.
     * @param content    The text content to convert to audio.
     * @param targetFile The path where the audio file should be saved.
     */
    void convertToAudio(String prompt, String content, Path targetFile);

}
