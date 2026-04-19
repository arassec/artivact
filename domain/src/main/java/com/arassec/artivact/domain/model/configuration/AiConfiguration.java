package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.TranslatableString;
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
     * The default translation prompt.
     */
    public static final String DEFAULT_TRANSLATION_PROMPT = "Translate the following text:";

    /**
     * The provider to use for translation.
     */
    private AiModel translationModel = AiModel.OPEN_AI;

    /**
     * The API key for translations.
     */
    private String translationApiKey;

    /**
     * Prompt template for translations.
     */
    private TranslatableString translationPrompt = new TranslatableString(DEFAULT_TRANSLATION_PROMPT);

    /**
     * The provider to use for text-to-speech.
     */
    private AiModel ttsModel = AiModel.OPEN_AI;

    /**
     * The API key for text-to-speech.
     */
    private String ttsApiKey;

    /**
     * Voice identifier for text-to-speech.
     */
    private TranslatableString ttsVoice = new TranslatableString("");

}
