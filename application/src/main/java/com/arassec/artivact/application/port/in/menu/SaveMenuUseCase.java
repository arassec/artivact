package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

public interface SaveMenuUseCase {

    /**
     * Saves a single menu.
     *
     * @param menu The menu to save.
     * @return All application menus, translated, and including the new menu.
     */
    List<Menu> saveMenu(Menu menu);

    /**
     * Saves the given menus.
     *
     * @param menus The menus to save.
     * @return The updated, translated menus after saving.
     */
    List<Menu> saveMenus(List<Menu> menus);

}
