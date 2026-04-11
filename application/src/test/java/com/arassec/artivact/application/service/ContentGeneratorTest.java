package com.arassec.artivact.application.service;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.widget.TextWidget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ContentGenerator}.
 */
class ContentGeneratorTest {

    private final ContentGenerator contentGenerator = new ContentGenerator() {
    };

    @Test
    void returnsFalseForNullOrBlankLocale() {
        assertThat(contentGenerator.isInvalidJavaLocale(null)).isFalse();
        assertThat(contentGenerator.isInvalidJavaLocale("")).isFalse();
        assertThat(contentGenerator.isInvalidJavaLocale("   ")).isFalse();
    }

    @Test
    void returnsFalseForValidLanguageOnlyLocale() {
        assertThat(contentGenerator.isInvalidJavaLocale("de")).isFalse();
    }

    @Test
    void returnsFalseForValidLocaleWithCountryAndUnderscoreSeparator() {
        assertThat(contentGenerator.isInvalidJavaLocale("de_DE")).isFalse();
    }

    @Test
    void returnsTrueForUnknownLanguage() {
        assertThat(contentGenerator.isInvalidJavaLocale("zz")).isTrue();
    }

    @Test
    void returnsTrueForUnknownCountry() {
        assertThat(contentGenerator.isInvalidJavaLocale("de_XX")).isTrue();
    }

    @Test
    void returnsTrueForIllFormedLocale() {
        assertThat(contentGenerator.isInvalidJavaLocale("de-123456789")).isTrue();
    }

    @Test
    void createsContentAudioAndStoresLocalizedAudioFilename() {
        TextWidget widget = new TextWidget();

        contentGenerator.processContentAudio("de", widget, "audio-de.mp3");

        assertThat(widget.getContentAudio()).isNotNull();
        assertThat(widget.getContentAudio().getValue()).isNull();
        assertThat(widget.getContentAudio().getTranslations()).containsEntry("de", "audio-de.mp3");
    }

    @Test
    void storesDefaultAudioFilenameWhenLocaleIsNullOrBlank() {
        TextWidget widgetWithNullLocale = new TextWidget();
        TextWidget widgetWithBlankLocale = new TextWidget();

        contentGenerator.processContentAudio(null, widgetWithNullLocale, "audio-null.mp3");
        contentGenerator.processContentAudio("   ", widgetWithBlankLocale, "audio-blank.mp3");

        assertThat(widgetWithNullLocale.getContentAudio()).isNotNull();
        assertThat(widgetWithNullLocale.getContentAudio().getValue()).isEqualTo("audio-null.mp3");
        assertThat(widgetWithNullLocale.getContentAudio().getTranslations()).isEmpty();

        assertThat(widgetWithBlankLocale.getContentAudio()).isNotNull();
        assertThat(widgetWithBlankLocale.getContentAudio().getValue()).isEqualTo("audio-blank.mp3");
        assertThat(widgetWithBlankLocale.getContentAudio().getTranslations()).isEmpty();
    }

    @Test
    void addsLocalizedAudioToExistingContentAudioWithoutOverwritingDefaultValue() {
        TextWidget widget = new TextWidget();
        TranslatableString contentAudio = new TranslatableString();
        contentAudio.setValue("audio.mp3");
        contentAudio.getTranslations().put("de", "audio-de.mp3");
        widget.setContentAudio(contentAudio);

        contentGenerator.processContentAudio("fr", widget, "audio-fr.mp3");

        assertThat(widget.getContentAudio()).isSameAs(contentAudio);
        assertThat(widget.getContentAudio().getValue()).isEqualTo("audio.mp3");
        assertThat(widget.getContentAudio().getTranslations())
                .containsEntry("de", "audio-de.mp3")
                .containsEntry("fr", "audio-fr.mp3");
    }

}
