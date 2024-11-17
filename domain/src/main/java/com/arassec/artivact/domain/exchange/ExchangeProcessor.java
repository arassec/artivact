package com.arassec.artivact.domain.exchange;

public interface ExchangeProcessor {

    /**
     * File suffix for packed export files.
     */
    String ZIP_FILE_SUFFIX = ".zip";

    /**
     * File suffix for exported content.
     */
    String CONTENT_EXCHANGE_SUFFIX = ".artivact.content";

    /**
     * Filename of the properties export JSON file.
     */
    String PROPERTIES_EXCHANGE_FILENAME_JSON = "artivact.properties-configuration.json";

    /**
     * Filename of the tags export JSON file.
     */
    String TAGS_EXCHANGE_FILENAME_JSON = "artivact.tags-configuration.json";

    /**
     * Filename of the main export file.
     */
    String CONTENT_EXCHANGE_MAIN_DATA_FILENAME_JSON = "artivact.content.json";

    /**
     * Filename for exported items JSON data files.
     */
    String ITEM_EXCHANGE_FILENAME_JSON = "artivact.item.json";

    /**
     * File suffix for exported menus.
     */
    String MENU_EXCHANGE_FILE_SUFFIX = ".artivact.menu.json";

    /**
     * File suffix for exported pages.
     */
    String PAGE_EXCHANGE_FILE_SUFFIX = ".artivact.page-content.json";

    /**
     * File suffix for exported search results.
     */
    String SEARCH_RESULT_FILE_SUFFIX = ".artivact.search-result.json";

}
