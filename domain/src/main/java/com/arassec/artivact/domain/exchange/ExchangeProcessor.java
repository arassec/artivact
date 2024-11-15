package com.arassec.artivact.domain.exchange;

public interface ExchangeProcessor {

    /**
     * File suffix for packed export files.
     */
    String ZIP_FILE_SUFFIX = ".zip";

    /**
     * File suffix for exported content.
     */
    String CONTENT_EXPORT_SUFFIX = ".artivact.content";

    /**
     * Filename of the properties export JSON file.
     */
    String PROPERTIES_EXPORT_FILENAME_JSON = "artivact.properties-configuration.json";

    /**
     * Filename of the tags export JSON file.
     */
    String TAGS_EXPORT_FILENAME_JSON = "artivact.tags-configuration.json";

    /**
     * Filename of the main export file.
     */
    String CONTENT_EXPORT_FILENAME_JSON = "artivact.content.json";

    /**
     * Filename for exported items JSON data files.
     */
    String ITEM_EXPORT_FILENAME_JSON = "artivact.item.json";

}
