package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.exchange.ImportContext;

import java.nio.file.Path;

/**
 * Use case for import item operations.
 */
public interface ImportItemUseCase {

    /**
     * Imports a previously exported artivact item.
     *
     * @param contentExport Path to the export file.
     * @param apiToken      The user's API token for authentication and authorization.
     */
    void importItem(Path contentExport, String apiToken);

    /**
     * Imports a previously exported artivact item.
     *
     * @param contentExport Path to the export.
     */
    void importItem(Path contentExport);

    /**
     * Imports item.
     *
     * @param importContext The import context.
     * @param itemId The item ID.
     */
    void importItem(ImportContext importContext, String itemId);

}
