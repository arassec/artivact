package com.arassec.artivact.web.model;

import com.arassec.artivact.core.model.appearance.ColorTheme;
import com.arassec.artivact.core.model.appearance.License;
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

}
