package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.domain.exchange.model.ExchangeMainData;

import java.nio.file.Path;

/**
 * Defines an exporter for Artivact's data.
 */
public interface ArtivactExporter {

    /**
     * Exports parts of or the complete collection.
     *
     * @param collectionExport The collection export's configuration.
     * @param menu             The menu this export is based on.
     * @return Path to the created export file.
     */
    Path exportCollection(CollectionExport collectionExport, Menu menu);

    /**
     * Exports the given menu and all its content.
     *
     * @param exportConfiguration The export's configuration.
     * @param menu                The menu to export.
     * @return The path to the newly created export.
     */
    Path exportMenu(ExportConfiguration exportConfiguration, Menu menu);

    /**
     * Exports a single item.
     *
     * @param item The item to export.
     * @return The path to the newly created export.
     */
    Path exportItem(Item item);

    /**
     * Exports the current property configuration.
     *
     * @return Path to the export.
     */
    Path exportPropertiesConfiguration();

    /**
     * Exports the current tag configuration.
     *
     * @return Path to the export.
     */
    Path exportTagsConfiguration();

    /**
     * Reads the main exchange data from the provided path.
     *
     * @param path Path to an export ZIP file containing the data file.
     * @return The read {@link ExchangeMainData}.
     */
    ExchangeMainData readExchangeMainData(Path path);

}
