package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for delete collection export operations.
 */
public interface DeleteCollectionExportUseCase {

    /**
     * Deletes the collection export with the given ID.
     *
     * @param id The ID of the collection export to delete.
     */
    void delete(String id);

}
