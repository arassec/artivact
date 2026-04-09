package com.arassec.artivact.application.port.in.ai;

/**
 * Use case for translating text using artificial intelligence.
 */
public interface TranslateTextUseCase {

    /**
     * Translates the given text into the specified target locale using AI.
     *
     * @param text         The text to translate.
     * @param targetLocale The target locale for the translation.
     * @return The translated text.
     */
    String translateText(String text, String targetLocale);

}
