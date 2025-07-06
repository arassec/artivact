package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.exchange.ImportContext;

public interface ImportMenuUseCase {

    /**
     * Imports a menu.
     *
     * @param importContext The import context.
     * @param menuId        The menu's ID.
     */
    void importMenu(ImportContext importContext, String menuId, boolean saveMenu);

}
