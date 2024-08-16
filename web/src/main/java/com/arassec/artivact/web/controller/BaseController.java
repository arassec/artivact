package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.web.model.OperationProgress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;

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

    /**
     * Converts a {@link ProgressMonitor} into a {@link OperationProgress}.
     *
     * @param progressMonitor The progress monitor to convert.
     * @return An operation progress containing the monitor's data.
     */
    protected ResponseEntity<OperationProgress> convert(ProgressMonitor progressMonitor) {
        if (progressMonitor != null) {
            OperationProgress operationProgress = new OperationProgress();
            operationProgress.setKey(progressMonitor.getLabelKey());
            operationProgress.setCurrentAmount(progressMonitor.getCurrentAmount());
            operationProgress.setTargetAmount(progressMonitor.getTargetAmount());
            if (progressMonitor.getException() != null) {
                operationProgress.setError(ExceptionUtils.getStackTrace(progressMonitor.getException()));
            }
            return ResponseEntity.ok(operationProgress);
        }
        return ResponseEntity.ok().build();
    }

}
