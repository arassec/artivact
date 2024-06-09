package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The two-column Image-Text widget contains an image on the left side, and a text on the right.
 */
@Getter
@Setter
public class ImageTextWidget extends Widget implements FileProcessingWidget {

    /**
     * The image.
     */
    private String image;

    /**
     * The text.
     */
    private TranslatableString text;

    /**
     * Enables or disables fullscreen availability.
     */
    private boolean fullscreenAllowed;

    /**
     * Creates a new instance.
     */
    public ImageTextWidget() {
        super(WidgetType.IMAGE_TEXT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename) {
        this.image = filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        if (image != null) {
            return List.of(image);
        }
        return List.of();
    }

}
