package com.arassec.artivact.adapter.in.rest.controller;

import lombok.extern.slf4j.Slf4j;

/**
 * Base for REST-Controllers with utility methods.
 */
@Slf4j
public abstract class BaseController {

    /**
     * Cache header value.
     */
    public static final String NO_CACHE = "no-cache";

    /**
     * Attachment header prefix.
     */
    public static final String ATTACHMENT_PREFIX = "attachment; filename=";

    /**
     * Expires header value.
     */
    public static final String EXPIRES_IMMEDIATELY = "0";

    /**
     * Mime type for ZIP files.
     */
    public static final String TYPE_ZIP = "application/zip";

    /**
     * Create the URL to an image with the given filename.
     *
     * @param itemId   The item's ID.
     * @param filename The file's name.
     * @return The (relative) URL as string.
     */
    protected String createImageUrl(String itemId, String filename) {
        return createUrl(itemId, filename, "image");
    }

    /**
     * Creates a URL to a given file of an item.
     *
     * @param itemId   The item's ID.
     * @param fileName The name of the file.
     * @param fileType The type of file, i.e. 'image' or 'model'.
     * @return The (relative) URL as string.
     */
    protected String createUrl(String itemId, String fileName, String fileType) {
        return "/api/item/" + itemId + "/" + fileType + "/" + fileName;
    }

}
