package com.arassec.artivact.application.port.in.collection;

import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.menu.Menu;

import java.nio.file.Path;

/**
 * Use case for export collection operations.
 */
public interface ExportCollectionUseCase {

    /**
     * Exports parts of or the complete collection.
     *
     * @param collectionExport The collection export's configuration.
     * @param menu             The menu this export is based on.
     * @return Path to the created export file.
     */
    Path exportCollection(CollectionExport collectionExport, Menu menu);

}
