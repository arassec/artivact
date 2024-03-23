package com.arassec.artivact.backend.service.model;

import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A translatable string.
 */
@Getter
@Setter
public class TranslatableString implements TranslatableObject {

    /**
     * The original value.
     */
    private String value;

    /**
     * The translated value.
     */
    @Transient
    private String translatedValue;

    /**
     * Configured translations for the value, indexed by locale.
     */
    private Map<String, String> translations = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void translate(String locale) {
        if (!StringUtils.hasText(locale)) {
            translatedValue = value;
            return;
        }
        if (translations.containsKey(locale)) {
            translatedValue = translations.get(locale);
            return;
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (locale.startsWith(entry.getKey() + "_")) {
                translatedValue = entry.getValue();
                return;
            }
        }
        translatedValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void translate(Locale locale) {
        translate(locale.toString());
    }

}
