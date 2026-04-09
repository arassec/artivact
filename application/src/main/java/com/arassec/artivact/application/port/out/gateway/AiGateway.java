package com.arassec.artivact.application.port.out.gateway;

import com.arassec.artivact.domain.model.configuration.AiConfiguration;

import java.nio.file.Path;

/**
 * Gateway for AI-based operations.
 */
public interface AiGateway {

    /**
     * Executes the given prompt.
     *
     * @param aiConfiguration The current AI configuration.
     * @param prompt          The prompt to execute.
     * @return The AI's result.
     */
    String execute(AiConfiguration aiConfiguration, String prompt);

    /**
     * Converts the given content to an audio file using text-to-speech.
     *
     * @param aiConfiguration The current AI configuration.
     * @param prompt          The prompt to guide the TTS generation.
     * @param targetFile      The path where the audio file should be saved.
     */
    void convertToAudio(AiConfiguration aiConfiguration, String prompt, Path targetFile);

}
