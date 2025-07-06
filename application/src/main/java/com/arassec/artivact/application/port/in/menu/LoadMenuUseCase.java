package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

public interface LoadMenuUseCase {

    /**
     * Returns the menu with the given ID, be it main- or sub-menu.
     *
     * @param menuId The ID of the menu to find.
     * @return The menu with the given ID.
     */
    Menu loadMenu(String menuId);

    /**
     * Loads translated menus.
     *
     * @return The currently configured, translated application menus.
     */
    List<Menu> loadTranslatedRestrictedMenus();

}
