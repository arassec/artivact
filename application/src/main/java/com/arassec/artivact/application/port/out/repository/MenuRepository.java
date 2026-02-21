package com.arassec.artivact.application.port.out.repository;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

/**
 * Repository for menus.
 */
public interface MenuRepository {

    /**
     * Loads all {@link Menu}s.
     *
     * @return List of all menus.
     */
    List<Menu> load();

    /**
     * Saves a single {@link Menu}.
     *
     * @param menu The menu to save.
     */
    void save(Menu menu);

    /**
     * Deletes the menu with the given ID.
     *
     * @param menuId The ID of the menu to delete.
     */
    void deleteById(String menuId);

}
