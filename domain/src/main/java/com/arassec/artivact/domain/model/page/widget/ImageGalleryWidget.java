package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.FileProcessingOperation;
import com.arassec.artivact.domain.model.page.FileProcessingWidget;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
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
     * If set to {@code true}, the widget's images will be displayed as icons, i.e., smaller.
     */
    private boolean iconMode;

    /**
     * If set to {@code true}, the widget's border is hidden.
     */
    private boolean hideBorder;

    /**
     * If set to {@code true}, the widget's images will be stretched to fill the available space.
     */
    private boolean stretchImages;

    /**
     * Creates a new instance.
     */
    public ImageGalleryWidget() {
        super(WidgetType.IMAGE_GALLERY);
    }

    /**
     * Creates a new instance.
     */
    @SuppressWarnings("java:S107") // This constructor is required as fallback for Jackson JSON deserialization.
    public ImageGalleryWidget(WidgetType type, TranslatableString heading, TranslatableString content,
                              List<String> images, boolean fullscreenAllowed, String textPosition,
                              boolean iconMode, boolean hideBorder, boolean stretchImages) {
        super(type);
        this.heading = heading;
        this.content = content;
        if (images != null) {
            this.images = images;
        }
        this.fullscreenAllowed = fullscreenAllowed;
        this.textPosition = textPosition;
        this.iconMode = iconMode;
        this.hideBorder = hideBorder;
        this.stretchImages = stretchImages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename, FileProcessingOperation operation) {
        if (FileProcessingOperation.ADD.equals(operation) && !images.contains(filename)) {
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
