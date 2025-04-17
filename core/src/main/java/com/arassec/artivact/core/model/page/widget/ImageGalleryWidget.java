package com.arassec.artivact.core.model.page.widget;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.page.FileProcessingOperation;
import com.arassec.artivact.core.model.page.FileProcessingWidget;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * An image gallery with additional heading and text content.
 */
@Getter
@Setter
public class ImageGalleryWidget extends Widget implements FileProcessingWidget {

    /**
     * The heading of the widget.
     */
    private TranslatableString heading;

    /**
     * The text content of the widget.
     */
    private TranslatableString content;

    /**
     * The list of image files of this gallery.
     */
    private List<String> images = new LinkedList<>();

    /**
     * Enables or disables fullscreen availability for the images.
     */
    private boolean fullscreenAllowed;

    /**
     * Defines where the widget's text should be positioned. Possible values supported in
     * the frontend are "TOP", "LEFT" and "RIGHT".
     */
    private String textPosition;

    /**
     * Creates a new instance.
     */
    public ImageGalleryWidget() {
        super(WidgetType.IMAGE_GALLERY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename, FileProcessingOperation operation) {
        if (FileProcessingOperation.ADD.equals(operation)) {
            images.add(filename);
        } else if (FileProcessingOperation.REMOVE.equals(operation)) {
            images.remove(filename);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        return images;
    }

}
