package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.ai.TranslateTextUseCase;
import org.springframework.stereotype.Service;

/**
 * Service for AI-based operations.
 */
@Service
public class AiService implements TranslateTextUseCase {

    /**
     * {@inheritDoc}
     */
    @Override
    public String translateText(String text, String targetLocale) {
        // AI translation not yet implemented.
        return text;
    }

}
