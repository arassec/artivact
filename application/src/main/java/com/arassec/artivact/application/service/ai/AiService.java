package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.ai.TranslateTextUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for AI-based operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
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
