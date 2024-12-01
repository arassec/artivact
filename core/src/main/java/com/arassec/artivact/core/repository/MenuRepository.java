package com.arassec.artivact.core.repository;

import com.arassec.artivact.core.model.configuration.MenuConfiguration;

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
