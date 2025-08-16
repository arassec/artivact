package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.exchange.ImportContext;

import java.nio.file.Path;

public interface ImportMenuUseCase {

    /**
     * Imports previously exported artivact menu.
     *
     * @param contentExport Path to the export.
     */
    void importMenu(Path contentExport);

    /**
     * Imports a menu.
     *
     * @param importContext The import context.
     * @param menuId        The menu's ID.
     */
    void importMenu(ImportContext importContext, String menuId, boolean saveMenu);

}
