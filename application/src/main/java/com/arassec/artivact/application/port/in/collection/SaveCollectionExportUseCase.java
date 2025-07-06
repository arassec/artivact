package com.arassec.artivact.application.port.in.collection;

import com.arassec.artivact.domain.model.exchange.CollectionExport;

import java.util.List;

public interface SaveCollectionExportUseCase {

    /**
     * Creates a new collection export.
     *
     * @param collectionExport The collection export to create.
     */
    void save(CollectionExport collectionExport);

    /**
     * Saves the sort order of the supplied collection exports.
     *
     * @param collectionExports Collection exports to save.
     */
    void saveSortOrder(List<CollectionExport> collectionExports);

}
