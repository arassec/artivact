package com.arassec.artivact.application.port.in.menu;

/**
 * Use case to relocate a menu to a different parent.
 */
public interface RelocateMenuUseCase {

    /**
     * Relocates a menu to a different parent menu.
     *
     * @param menuId          The menu's ID.
     * @param newParentMenuId The new parent menu's ID.
     */
    void relocateMenu(String menuId, String newParentMenuId);

}
