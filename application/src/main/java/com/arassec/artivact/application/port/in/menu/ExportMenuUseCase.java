package com.arassec.artivact.application.port.in.menu;

import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;

import java.nio.file.Path;

public interface ExportMenuUseCase {

    /**
     * Exports a menu with the associated page and referenced items.
     *
     * @param menuId The ID of the menu to export.
     * @return Path to the exported ZIP file.
     */
    Path exportMenu(String menuId);

    /**
     * Exports a menu with the associated page and referenced items.
     *
     * @param exportContext Context of the export.
     * @param menu          The menu to export.
     */
    void exportMenu(ExportContext exportContext, Menu menu);

}
