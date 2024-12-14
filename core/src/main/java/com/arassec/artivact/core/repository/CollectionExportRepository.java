package com.arassec.artivact.core.repository;

import com.arassec.artivact.core.model.exchange.CollectionExport;

import java.util.List;
import java.util.Optional;

/**
 * Repository for collection exports.
 */
public interface CollectionExportRepository {

    /**
     * Returns the collection export with the given ID.
     *
     * @param id ID of the collection export to load.
     * @return The {@link CollectionExport} with the given ID.
     */
    Optional<CollectionExport> findById(String id);

    /**
     * Returns all existing collection exports.
     *
     * @return List of available {@link CollectionExport}s.
     */
    List<CollectionExport> findAll();

    /**
     * Saves the given collection export.
     *
     * @param contentExport The collection export to save.
     */
    void save(CollectionExport contentExport);

    /**
     * Deletes the collection export with the given ID.
     *
     * @param id The ID of the collection export to delete.
     */
    void delete(String id);

    /**
     * Saves the sort order of the supplied collection exports.
     *
     * @param collectionExports The exports to save the sort order of.
     */
    void saveSortOrder(List<CollectionExport> collectionExports);

}
