package com.arassec.artivact.application.service;

import java.util.*;

public interface LocaleValidator {

    Set<String> ISO_LANGUAGES =
            new HashSet<>(Arrays.asList(Locale.getISOLanguages()));

    Set<String> ISO_COUNTRIES =
            new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    default boolean isValidJavaLocale(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        String normalized = input.trim().replace('_', '-');

        try {
            Locale locale = new Locale.Builder()
                    .setLanguageTag(normalized)
                    .build();

            String language = locale.getLanguage();
            String country = locale.getCountry();

            if (language.isEmpty() || !ISO_LANGUAGES.contains(language)) {
                return false;
            }

            if (!country.isEmpty() && !ISO_COUNTRIES.contains(country)) {
                return false;
            }

            return true;
        } catch (IllformedLocaleException e) {
            return false;
        }
    }
}
