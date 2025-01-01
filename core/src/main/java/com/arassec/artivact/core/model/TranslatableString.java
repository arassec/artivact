package com.arassec.artivact.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A translatable string.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class TranslatableString implements TranslatableObject {

    /**
     * The original value.
     */
    private String value;

    /**
     * The translated value.
     */
    private String translatedValue;

    /**
     * Configured translations for the value, indexed by locale.
     */
    @Builder.Default
    private Map<String, String> translations = new HashMap<>();

    /**
     * Creates a new instance.
     * <p>
     * This constructor is required as fallback for Jackson JSON deserialization. In case a string should be treated
     * as {@link TranslatableString} after being persisted already, this constructor will take care of the conversion
     * on the fly.
     *
     * @param value The untranslated value.
     */
    public TranslatableString(String value) {
        this.value = value;
        this.translations = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void translate(String locale) {
        if (locale == null || locale.isEmpty()) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.translatedValue = null;
    }

}
