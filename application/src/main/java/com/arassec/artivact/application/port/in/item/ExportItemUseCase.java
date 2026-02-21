package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;

import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Use case for export item operations.
 */
public interface ExportItemUseCase {

    /**
     * Exports a single item into a ZIP file.
     *
     * @param itemId The item's ID.
     * @return The path to the newly created export ZIP file.
     */
    Path exportItem(String itemId);

    /**
     * Exports a single item into a ZIP file.
     *
     * @param item The item to export.
     * @return The path to the newly created export ZIP file.
     */
    Path exportItem(Item item);

    /**
     * Exports an item using an existing {@link ExportContext}.
     *
     * @param exportContext Export context containing the export target directory and export configuration.
     * @param item          The item to export.
     */
    void exportItem(ExportContext exportContext, Item item);

    /**
     * Copies a created export to an output stream and deletes it afterward.
     *
     * @param export       The export.
     * @param outputStream The target stream.
     */
    void copyExportAndDelete(Path export, OutputStream outputStream);

}
