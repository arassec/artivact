package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

/**
 * Use case for add page to menu operations.
 */
public interface AddPageToMenuUseCase {

    /**
     * Adds a new page to the menu with the given ID.
     *
     * @param menuId The ID of the menu to which the page should be added.
     * @return The updated list of menus.
     */
    List<Menu> addPageToMenu(String menuId);

}
