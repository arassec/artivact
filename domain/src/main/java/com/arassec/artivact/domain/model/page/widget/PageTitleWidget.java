package com.arassec.artivact.domain.model.page.widget;

import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.page.FileProcessingOperation;
import com.arassec.artivact.domain.model.page.FileProcessingWidget;
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
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
    public void processFile(String filename, FileProcessingOperation operation) {
        if (FileProcessingOperation.ADD.equals(operation)) {
            this.backgroundImage = filename;
        } else if (FileProcessingOperation.REMOVE.equals(operation)) {
            this.backgroundImage = null;
        }
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
