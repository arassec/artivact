package com.arassec.artivact.domain.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for artificial intelligence features.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiConfiguration {

    /**
     * Whether AI features are enabled.
     */
    private boolean enabled;

    /**
     * The API key for the AI service.
     */
    private String apiKey;

    /**
     * Prompt template for translations.
     */
    private String translationPrompt = "Translate the following text into the locale '{locale}':";

    /**
     * Voice identifier for text-to-speech.
     */
    private String ttsVoice = "alloy";

}
