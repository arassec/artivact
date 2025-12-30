package com.arassec.artivact.application.port.in.collection;

import com.arassec.artivact.domain.model.exchange.CollectionExport;

import java.util.List;

/**
 * Use case for load collection export operations.
 */
public interface LoadCollectionExportUseCase {

    /**
     * Returns all collection exports available to the current user.
     *
     * @return All available collection exports.
     */
    List<CollectionExport> loadAllRestricted();

    /**
     * Returns all available collection exports.
     *
     * @return All available collection exports.
     */
    List<CollectionExport> loadAll();

    /**
     * Loads a single collection export.
     *
     * @param id The collection export's ID.
     * @return The collection export.
     */
    CollectionExport load(String id);

}
