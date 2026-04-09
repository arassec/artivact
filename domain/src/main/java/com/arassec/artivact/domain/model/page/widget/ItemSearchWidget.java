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
 * Displays search input and the result items of the search in a grid.
 */
@Getter
@Setter
public class ItemSearchWidget extends Widget implements FileProcessingWidget {

    /**
     * The heading, displayed above the search result.
     */
    private TranslatableString heading;

    /**
     * The text content, displayed below the heading but above the search result.
     */
    private TranslatableString content;

    /**
     * The filename of the audio version of the content.
     */
    private String contentAudio;

    /**
     * The search term to perform.
     */
    private String searchTerm;

    /**
     * Maximum number of results.
     */
    private int maxResults;

    /**
     * Size of the search result page.
     */
    private int pageSize;

    /**
     * Creates a new instance.
     */
    public ItemSearchWidget() {
        super(WidgetType.ITEM_SEARCH);
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
