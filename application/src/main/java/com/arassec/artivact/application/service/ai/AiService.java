package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.ai.TranslateTextUseCase;
import com.arassec.artivact.application.port.out.gateway.AiGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for AI-based operations.
 */
@Service
@RequiredArgsConstructor
public class AiService implements TranslateTextUseCase {

    /**
     * The AI gateway for interacting with the AI provider.
     */
    private final AiGateway aiGateway;

    /**
     * {@inheritDoc}
     */
    @Override
    public String translateText(String text, String targetLocale) {
        return aiGateway.translate(text, targetLocale);
    }

}
