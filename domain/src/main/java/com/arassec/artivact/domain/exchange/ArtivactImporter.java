package com.arassec.artivact.domain.exchange;

import java.nio.file.Path;

/**
 * Defines an importer for Artivact's data.
 */
public interface ArtivactImporter {

    /**
     * Imports previously exported artivact content.
     *
     * @param contentExport Path to the export.
     */
    void importContent(Path contentExport);

}
