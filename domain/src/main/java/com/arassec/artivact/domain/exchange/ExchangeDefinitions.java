package com.arassec.artivact.domain.exchange;

/**
 * Contains constants regarding the exchange of Artivact data.
 */
public final class ExchangeDefinitions {

    /**
     * Creates a new instance.
     */
    private ExchangeDefinitions() {
        // prevent instantiation!
    }

    /**
     * File suffix for packed export files.
     */
    public static final String ZIP_FILE_SUFFIX = ".zip";

    /**
     * File suffix for exported content.
     */
    public static final String COLLECTION_EXCHANGE_SUFFIX = ".artivact.collection";

    /**
     * Filename of the properties export JSON file.
     */
    public static final String PROPERTIES_EXCHANGE_FILENAME_JSON = "artivact.properties-configuration.json";

    /**
     * Filename of the tags export JSON file.
     */
    public static final String TAGS_EXCHANGE_FILENAME_JSON = "artivact.tags-configuration.json";

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
     * Filename for exported items JSON data files.
     */
    public static final String ITEM_EXCHANGE_FILENAME_JSON = "artivact.item.json";

    /**
     * File suffix for exported menus.
     */
    public static final String MENU_EXCHANGE_FILE_SUFFIX = ".artivact.menu.json";

    /**
     * File suffix for exported pages.
     */
    public static final String PAGE_EXCHANGE_FILE_SUFFIX = ".artivact.page-content.json";

    /**
     * File suffix for exported search results.
     */
    public static final String SEARCH_RESULT_FILE_SUFFIX = ".artivact.search-result.json";

}
