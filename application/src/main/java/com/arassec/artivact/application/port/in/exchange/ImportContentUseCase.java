package com.arassec.artivact.application.port.in.exchange;

import java.nio.file.Path;

public interface ImportContentUseCase {

    /**
     * Imports previously exported artivact content, e.g. menus or items.
     *
     * @param contentExport Path to the export.
     */
    void importContent(Path contentExport);

    /**
     * Imports previously exported artivact content, e.g. menus or items.
     *
     * @param contentExport Path to the export.
     * @param apiToken      The user's API token.
     */
    void importContent(Path contentExport, String apiToken);

}
