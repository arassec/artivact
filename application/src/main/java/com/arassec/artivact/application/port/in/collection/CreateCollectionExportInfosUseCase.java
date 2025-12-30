package com.arassec.artivact.application.port.in.collection;

import com.arassec.artivact.domain.model.exchange.CollectionExport;

import java.io.OutputStream;
import java.util.List;

/**
 * Use case for create collection export infos operations.
 */
public interface CreateCollectionExportInfosUseCase {

    /**
     * Loads the overview of all available collection exports and writes it to the given output stream.
     *
     * @param targetOutputStream The output stream to write the collection export infos to.
     * @param collectionExports  The collection exports to convert and return.
     */
    void createCollectionExportInfos(OutputStream targetOutputStream, List<CollectionExport> collectionExports);

}
