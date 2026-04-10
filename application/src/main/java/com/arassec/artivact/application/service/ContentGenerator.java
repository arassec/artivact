package com.arassec.artivact.application.service;

import com.arassec.artivact.domain.model.ContentAudioProvider;
import com.arassec.artivact.domain.model.TranslatableString;
import org.springframework.util.StringUtils;

import java.util.*;

public interface ContentGenerator {

    Set<String> ISO_LANGUAGES =
            new HashSet<>(Arrays.asList(Locale.getISOLanguages()));

    Set<String> ISO_COUNTRIES =
            new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    default boolean isInvalidJavaLocale(String input) {
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
                return true;
            }

            return !country.isEmpty() && ISO_COUNTRIES.contains(country);

        } catch (IllformedLocaleException e) {
            return true;
        }
    }

    /**
     * Processes a content audio provider's audio content.
     *
     * @param locale               The current locale.
     * @param contentAudioProvider The object process.
     * @param audioFilename        The name of the audio file to process.
     */
    default void processContentAudio(String locale, ContentAudioProvider contentAudioProvider, String audioFilename) {
        TranslatableString contentAudio = contentAudioProvider.getContentAudio();
        if (contentAudio == null) {
            contentAudio = new TranslatableString();
            contentAudioProvider.setContentAudio(contentAudio);
        }
        if (StringUtils.hasText(locale)) {
            contentAudio.getTranslations().put(locale, audioFilename);
        } else {
            contentAudio.setValue(audioFilename);
        }
    }

}
