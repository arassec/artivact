package com.arassec.artivact.application.port.out.gateway;

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

}
