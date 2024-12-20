package com.arassec.artivact.core.model.page.widget;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Displays search input and the result items of the search in a grid.
 */
@Getter
@Setter
public class ItemSearchWidget extends Widget {

    /**
     * The heading, displayed above the search result.
     */
    private TranslatableString heading;

    /**
     * The text content, displayed below the heading but above the search result.
     */
    private TranslatableString content;

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

}
