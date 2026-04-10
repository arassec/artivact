package com.arassec.artivact.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TranslatableString}.
 */
class TranslatableStringTest {

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

    /**
     * Tests that the defaultLocale is used as fallback when the user's locale is not available.
     */
    @Test
    void testTranslateWithDefaultLocaleFallback() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt", "fr", "Objet"));

        translatableString.translate("ja", "de");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Objekt");
    }

    /**
     * Tests that the user's locale takes priority over the defaultLocale.
     */
    @Test
    void testTranslateWithDefaultLocaleUserLocaleTakesPriority() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt", "fr", "Objet"));

        translatableString.translate("fr", "de");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Objet");
    }

    /**
     * Tests that the global default value is used when neither user locale nor defaultLocale matches.
     */
    @Test
    void testTranslateWithDefaultLocaleFallsBackToValue() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt"));

        translatableString.translate("ja", "fr");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");
    }

    /**
     * Tests that null or empty defaultLocale is handled gracefully.
     */
    @Test
    void testTranslateWithNullDefaultLocale() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt"));

        translatableString.translate("ja", null);
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");

        translatableString.translate("ja", "");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");
    }

    /**
     * Tests that null or empty locale with defaultLocale falls back to the value.
     */
    @Test
    void testTranslateWithNullLocaleAndDefaultLocale() {
        TranslatableString translatableString = new TranslatableString("Item");
        translatableString.setTranslations(Map.of("de", "Objekt"));

        translatableString.translate(null, "de");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");

        translatableString.translate("", "de");
        assertThat(translatableString.getTranslatedValue()).isEqualTo("Item");
    }

}
