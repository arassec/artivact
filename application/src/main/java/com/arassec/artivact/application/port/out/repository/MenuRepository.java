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
     * @param menu  The menu to save.
     * @param index The index for ordering.
     */
    void save(Menu menu, int index);

    /**
     * Deletes menus whose IDs are not in the given set.
     *
     * @param menuIds The IDs of menus to keep.
     */
    void deleteWhereIdNotIn(java.util.Set<String> menuIds);

    /**
     * Deletes all menus.
     */
    void deleteAll();

}
