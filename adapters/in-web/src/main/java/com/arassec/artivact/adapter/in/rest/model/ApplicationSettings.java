package com.arassec.artivact.adapter.in.rest.model;

import com.arassec.artivact.domain.model.appearance.ColorTheme;
import com.arassec.artivact.domain.model.appearance.License;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * The application's appearance settings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationSettings {

    /**
     * The application title.
     */
    private String applicationTitle;

    /**
     * The available locales as configured by an admin and maintained by the users.
     */
    @Builder.Default
    private List<String> availableLocales = new LinkedList<>();

    /**
     * The locale used by the application for the current user.
     */
    private String applicationLocale;

    /**
     * The default locale used as fallback for translatable strings.
     */
    private String defaultLocale;

    /**
     * The application's color theme.
     */
    private ColorTheme colorTheme;

    /**
     * The license configuration.
     */
    private License license;

    /**
     * The profiles the application is started with.
     */
    private Profiles profiles;

    /**
     * List of available roles of the application.
     */
    @Builder.Default
    private List<String> availableRoles = new LinkedList<>();

    /**
     * Indicates whether synchronization to a remote Artivact instance is available or not.
     */
    private boolean syncAvailable;

    /**
     * Indicates whether AI features are enabled.
     */
    private boolean aiEnabled;

}
