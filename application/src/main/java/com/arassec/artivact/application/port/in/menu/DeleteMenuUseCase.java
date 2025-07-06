package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

public interface DeleteMenuUseCase {

    /**
     * Deletes a menu.
     *
     * @param menuId The ID of the menu to delete.
     * @return All remaining application menus, translated.
     */
    List<Menu> deleteMenu(String menuId);

}
