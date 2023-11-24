package com.arassec.artivact.backend.service.model;

import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
public class TranslatableString implements TranslatableItem {

    private String value;

    @Transient
    private String translatedValue;

    private Map<String, String> translations = new HashMap<>();

    @Override
    public String translate(String locale) {
        if (!StringUtils.hasText(locale)) {
            translatedValue = value;
            return translatedValue;
        }
        if (translations.containsKey(locale)) {
            translatedValue = translations.get(locale);
            return translatedValue;
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (locale.startsWith(entry.getKey() + "_")) {
                translatedValue = entry.getValue();
                return translatedValue;
            }
        }
        translatedValue = value;
        return translatedValue;
    }

    @Override
    public String translate(Locale locale) {
        return translate(locale.toString());
    }

}
