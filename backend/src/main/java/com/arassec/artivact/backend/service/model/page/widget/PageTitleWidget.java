package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.page.FileProcessingWidget;
import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Widget for page titles.
 */
@Getter
@Setter
public class PageTitleWidget extends Widget implements FileProcessingWidget {

    /**
     * The title of the page.
     */
    private TranslatableString title;

    /**
     * An optional background image for the title.
     */
    private String backgroundImage;

    /**
     * Creates a new instance.
     */
    public PageTitleWidget() {
        super(WidgetType.PAGE_TITLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename) {
        this.backgroundImage = filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        if (backgroundImage != null) {
            return List.of(backgroundImage);
        }
        return List.of();
    }

}
