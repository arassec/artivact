package com.arassec.artivact.domain.model;

import java.util.Locale;

/**
 * A translatable object.
 */
public interface TranslatableObject {

    /**
     * Translates the object using the given locale.
     *
     * @param locale The locale as String.
     */
    void translate(String locale);

    /**
     * Translates the object using the given locale with a fallback to the default locale.
     *
     * @param locale        The locale as String.
     * @param defaultLocale The default locale to use as fallback.
     */
    default void translate(String locale, String defaultLocale) {
        translate(locale);
    }

    /**
     * Translates the object using the given locale.
     *
     * @param locale The locale.
     */
    void translate(Locale locale);

    /**
     * Clears the current translation of the object.
     */
    void clear();
}
