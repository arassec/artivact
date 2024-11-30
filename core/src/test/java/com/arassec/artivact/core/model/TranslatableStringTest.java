package com.arassec.artivact.core.model;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TranslatableString}.
 */
public class TranslatableStringTest {

    /**
     * Tests fallback if no translations are given.
     */
    @Test
    void testTranslateNoTranslations() {
        TranslatableString translatableString = new TranslatableString("test");

        translatableString.translate((String) null);
        assertThat(translatableString.getTranslatedValue()).isEqualTo("test");

        translatableString.translate(Locale.of(""));
        assertThat(translatableString.getTranslatedValue()).isEqualTo("test");

        translatableString.translate(Locale.GERMAN);
        assertThat(translatableString.getTranslatedValue()).isEqualTo("test");
    }

    /**
     * Tests fallback if an unsupported locale is provided.
     */
    @Test
    void testTranslateUnsupportedLocale() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt"));

        translatableString.translate("ja");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");
    }

    /**
     * Tests translation if a supported locale is provided.
     */
    @Test
    void testTranslateSupportedLocale() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt"));

        translatableString.translate("de");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Objekt");
    }

}
