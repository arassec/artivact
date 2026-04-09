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
 * Displays a central text with an optional heading.
 */
@Getter
@Setter
public class TextWidget extends Widget implements FileProcessingWidget {

    /**
     * The heading.
     */
    private TranslatableString heading;

    /**
     * The text content.
     */
    private TranslatableString content;

    /**
     * The filename of the audio version of the content.
     */
    private String contentAudio;

    /**
     * Creates a new widget.
     */
    public TextWidget() {
        super(WidgetType.TEXT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFile(String filename, FileProcessingOperation operation) {
        if (FileProcessingOperation.ADD.equals(operation)) {
            this.contentAudio = filename;
        } else if (FileProcessingOperation.REMOVE.equals(operation)) {
            this.contentAudio = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> usedFiles() {
        List<String> files = new LinkedList<>();
        if (contentAudio != null) {
            files.add(contentAudio);
        }
        return files;
    }

}
