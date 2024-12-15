package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.exchange.CollectionExport;

import java.nio.file.Path;

/**
 * Defines an importer for Artivact's data.
 */
public interface ArtivactImporter {

    /**
     * Imports previously exported artivact content, e.g. menus or items.
     *
     * @param contentExport Path to the export.
     */
    void importContent(Path contentExport);

    /**
     * Imports a collection export.
     *
     * @param collectionExport The collection export.
     * @param distributionOnly Set to {@code true}, if the export should only be imported for further distribution. If
     *                         set to {@code false}, the collection content (i.e. menus, pages, items) will be imported,
     *                         too.
     * @return A newly created {@link CollectionExport}.
     */
    CollectionExport importCollection(Path collectionExport, boolean distributionOnly);

}
