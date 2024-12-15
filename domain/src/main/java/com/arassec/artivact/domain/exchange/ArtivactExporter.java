package com.arassec.artivact.domain.exchange;

import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.configuration.TagsConfiguration;
import com.arassec.artivact.core.model.exchange.CollectionExport;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.menu.Menu;

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
    Path exportCollection(CollectionExport collectionExport, Menu menu, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration);

    /**
     * Exports the given menu and all its content.
     *
     * @param exportConfiguration     The export's configuration.
     * @param menu                    The menu to export.
     * @param propertiesConfiguration The current property configuration.
     * @param tagsConfiguration       The current tags configuration.
     * @return The path to the newly created export.
     */
    Path exportMenu(ExportConfiguration exportConfiguration, Menu menu, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration);

    /**
     * Exports a single item.
     *
     * @param item                    The item to export.
     * @param propertiesConfiguration The current property configuration.
     * @param tagsConfiguration       The current tags configuration.
     * @return The path to the newly created export.
     */
    Path exportItem(Item item, PropertiesConfiguration propertiesConfiguration, TagsConfiguration tagsConfiguration);

    /**
     * Exports the current property configuration.
     *
     * @param propertiesConfiguration The current property configuration.
     * @return Path to the export.
     */
    Path exportPropertiesConfiguration(PropertiesConfiguration propertiesConfiguration);

    /**
     * Exports the current tag configuration.
     *
     * @param tagsConfiguration The current tags configuration.
     * @return Path to the export.
     */
    Path exportTagsConfiguration(TagsConfiguration tagsConfiguration);

}
