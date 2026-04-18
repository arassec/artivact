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
    public static final String DEFAULT_TRANSLATION_PROMPT = "Translate the following text into the locale '{locale}':";

    /**
     * The default OpenAI voice.
     */
    public static final String DEFAULT_TTS_VOICE = "alloy";

    /**
     * The provider to use for translation.
     */
    private AiModel translationModel = AiModel.OpenAI;

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
    private AiModel ttsModel = AiModel.OpenAI;

    /**
     * The API key for text-to-speech.
     */
    private String ttsApiKey;

    /**
     * Voice identifier for text-to-speech.
     */
    private TranslatableString ttsVoice = new TranslatableString(DEFAULT_TTS_VOICE);

    /**
     * Sets the translation prompt from a plain string for backwards compatibility.
     *
     * @param translationPrompt The translation prompt.
     */
    public void setTranslationPrompt(String translationPrompt) {
        this.translationPrompt = new TranslatableString(translationPrompt);
    }

    /**
     * Sets the translation prompt.
     *
     * @param translationPrompt The translation prompt.
     */
    public void setTranslationPrompt(TranslatableString translationPrompt) {
        this.translationPrompt = translationPrompt == null
                ? new TranslatableString(DEFAULT_TRANSLATION_PROMPT)
                : translationPrompt;
    }

    /**
     * Sets the text-to-speech voice from a plain string for backwards compatibility.
     *
     * @param ttsVoice The text-to-speech voice.
     */
    public void setTtsVoice(String ttsVoice) {
        this.ttsVoice = new TranslatableString(ttsVoice);
    }

    /**
     * Sets the text-to-speech voice.
     *
     * @param ttsVoice The text-to-speech voice.
     */
    public void setTtsVoice(TranslatableString ttsVoice) {
        this.ttsVoice = ttsVoice == null
                ? new TranslatableString(DEFAULT_TTS_VOICE)
                : ttsVoice;
    }

    /**
     * Sets a single legacy API key for both translation and text-to-speech.
     *
     * @param apiKey The legacy API key.
     */
    public void setApiKey(String apiKey) {
        if (translationApiKey == null) {
            translationApiKey = apiKey;
        }
        if (ttsApiKey == null) {
            ttsApiKey = apiKey;
        }
    }

    /**
     * Sets the translation model.
     *
     * @param translationModel The translation model.
     */
    public void setTranslationModel(AiModel translationModel) {
        this.translationModel = translationModel == null ? AiModel.OpenAI : translationModel;
    }

    /**
     * Sets the text-to-speech model.
     *
     * @param ttsModel The text-to-speech model.
     */
    public void setTtsModel(AiModel ttsModel) {
        this.ttsModel = ttsModel == null ? AiModel.OpenAI : ttsModel;
    }

}
