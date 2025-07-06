package com.arassec.artivact.application.port.in.configuration;

public interface CheckRuntimeConfigurationUseCase {

    /**
     * Returns whether the application is run in desktop-mode.
     *
     * @return {@code true} if the application is run in desktop-mode, {@code false} otherwise.
     */
    boolean isDesktopProfileEnabled();

    /**
     * Returns whether the application is run in E2E-mode.
     *
     * @return {@code true} if the application is run in E2E-mode, {@code false} otherwise.
     */
    boolean isE2eProfileEnabled();

}
