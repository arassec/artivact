package com.arassec.artivact.core.model;

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
     * Translates the object using the given locale.
     *
     * @param locale The locale.
     */
    void translate(Locale locale);

}
