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
     * General context prompt for the AI.
     */
    private String generalContext = "You are a museum curator responsible for the texts of the exhibits. Write factual, scientific, and simple for your audience.";

    /**
     * Prompt template for translations.
     */
    private String translationPrompt = "Translate the following text into the locale '{locale}'.";

    /**
     * Prompt template for text-to-speech generation.
     */
    private String ttsPrompt = "Create audio for the following text in locale '{locale}'.";

    /**
     * Voice identifier for text-to-speech.
     */
    private String ttsVoice = "alloy";

}
