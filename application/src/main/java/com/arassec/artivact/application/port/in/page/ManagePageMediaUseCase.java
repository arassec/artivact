package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.page.PageContent;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Use case for manage page media operations.
 */
public interface ManagePageMediaUseCase {

    /**
     * Saves a file for a given widget in the filesystem.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param file     The file to save.
     * @return The name of the saved file.
     */
    String saveFile(String pageId, String widgetId, MultipartFile file);

    /**
     * Loads a widget's file from the filesystem.
     *
     * @param widgetId   The widget's ID.
     * @param filename   The filename.
     * @param targetSize The desired target size (if an image is loaded).
     * @param wip Specifies whether the 'work-in-progress' version of the file should be loaded, or the productive one.
     * @return The file as {@link FileSystemResource}.
     */
    byte[] loadFile(String widgetId, String filename, ImageSize targetSize, boolean wip);

    /**
     * Deletes a file for a given widget from the filesystem.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param filename The file to delete.
     */
    PageContent deleteFile(String pageId, String widgetId, String filename);

}
