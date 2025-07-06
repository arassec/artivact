package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.configuration.MenuConfiguration;

/**
 * Repository for menus.
 */
public interface MenuRepository {

    /**
     * Loads the current {@link MenuConfiguration}.
     */
    MenuConfiguration load();

    /**
     * Saves the {@link MenuConfiguration}.
     */
    void save(MenuConfiguration menuConfiguration);

}
