package com.arassec.artivact.domain.model.misc;

/**
 * Contains constants regarding the exchange of Artivact data.
 */
public final class ExchangeDefinitions {

    /**
     * File suffix for packed export files.
     */
    public static final String ZIP_FILE_SUFFIX = ".zip";

    /**
     * File suffix for exported content.
     */
    public static final String COLLECTION_EXCHANGE_SUFFIX = ".artivact.collection";

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String COLLECTION_EXPORT_OVERVIEWS_ZIP_FILE = "artivact.collection-export-overviews" + ZIP_FILE_SUFFIX;

    /**
     * Filename of the content-export-overviews file.
     */
    public static final String COLLECTION_EXPORT_OVERVIEWS_JSON_FILE = "artivact.collection-export-overviews.json";

    /**
     * Filename of a configuration export JSON file.
     */
    public static final String CONFIGURATION_EXCHANGE_FILENAME_JSON = ".artivact.configuration.json";

    /**
     * Filename of the properties export JSON file.
     */
    public static final String PROPERTIES_EXCHANGE_FILENAME_JSON = "properties.artivact.configuration.json";

    /**
     * Filename of the tags export JSON file.
     */
    public static final String TAGS_EXCHANGE_FILENAME_JSON = "tags.artivact.configuration.json";

    /**
     * Filename of the menu export JSON file.
     */
    public static final String MENU_EXCHANGE_FILENAME_JSON = "artivact.menu.json";

    /**
     * Filename of the page export JSON file.
     */
    public static final String PAGE_EXCHANGE_FILENAME_JSON = "artivact.page.json";

    /**
     * Filename for exported items JSON data files.
     */
    public static final String ITEM_EXCHANGE_FILENAME_JSON = "artivact.item.json";

    /**
     * Filename for exported search results.
     */
    public static final String SEARCH_RESULT_FILENAME_JSON = "artivact.search-result.json";

    /**
     * Filename of the menu export ZIP file.
     */
    public static final String MENU_EXCHANGE_FILENAME_ZIP = "artivact.menu" + ZIP_FILE_SUFFIX;

    /**
     * Filename of the item export ZIP file.
     */
    public static final String ITEM_EXCHANGE_FILENAME_ZIP = "artivact.item" + ZIP_FILE_SUFFIX;

    /**
     * Filename of the main export file.
     */
    public static final String CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON = "artivact.content.json";

    /**
     * Creates a new instance.
     */
    private ExchangeDefinitions() {
        // prevent instantiation!
    }

}
