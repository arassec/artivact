package com.arassec.artivact.domain.exchange;

import java.nio.file.Path;

/**
 * Defines an importer for Artivact's data.
 */
public interface ArtivactImporter {

    /**
     * Imports a previously exported artivact item.
     *
     * @param itemExport Path to the export.
     */
    void importItem(Path itemExport);

}
