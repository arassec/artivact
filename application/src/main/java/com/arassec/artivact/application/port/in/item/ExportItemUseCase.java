package com.arassec.artivact.application.port.in.item;

import com.arassec.artivact.domain.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.domain.model.configuration.TagsConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;

import java.io.OutputStream;
import java.nio.file.Path;

public interface ExportItemUseCase {

    /**
     * Creates an item's export ZIP file.
     *
     * @param itemId The item's ID.
     */
    Path exportItem(String itemId);

    /**
     * Exports an item using the provided {@link ExportContext}.
     *
     * @param exportContext Export context containing the export target directory and export configuration.
     * @param item          The item to export.
     */
    void exportItem(ExportContext exportContext, Item item);

    /**
     * Exports a single item into a ZIP file.
     *
     * @param item                    The item to export.
     * @param propertiesConfiguration The current property configuration.
     * @param tagsConfiguration       The current tags configuration.
     * @return The path to the newly created export ZIP file.
     */
    Path exportItem(Item item, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration);

    /**
     * Copies a created export to an output stream and deletes it afterward.
     *
     * @param export       The export.
     * @param outputStream The target stream.
     */
    void copyExportAndDelete(Path export, OutputStream outputStream);

}
