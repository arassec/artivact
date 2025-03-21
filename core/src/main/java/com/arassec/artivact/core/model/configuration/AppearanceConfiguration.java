package com.arassec.artivact.core.model.configuration;

import com.arassec.artivact.core.model.appearance.ColorTheme;
import com.arassec.artivact.core.model.appearance.License;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the application's appearance.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppearanceConfiguration {

    /**
     * The application title.
     */
    private String applicationTitle;

    /**
     * The available locales as configured by an admin and maintained by the users.
     */
    private String availableLocales;

    /**
     * The locale used by the application for the current user.
     */
    private String applicationLocale;

    /**
     * The application's color theme.
     */
    private ColorTheme colorTheme;

    /**
     * The favicon as Base64 encoded string.
     */
    private String encodedFavicon;

    /**
     * The license configuration.
     */
    private License license;

}
