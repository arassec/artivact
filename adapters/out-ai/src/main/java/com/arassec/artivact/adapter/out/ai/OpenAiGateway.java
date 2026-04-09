package com.arassec.artivact.adapter.out.ai;

import com.arassec.artivact.application.port.out.gateway.AiGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

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
     * {@inheritDoc}
     */
    @Override
    public String translate(String text, String targetLocale) {
        log.debug("Translating text to locale '{}' via OpenAI.", targetLocale);

        String prompt = "Translate the following text into the locale '" + targetLocale
                + "'. Only return the translated text, nothing else.\n\n" + text;

        return openAiChatModel.call(prompt);
    }

}
