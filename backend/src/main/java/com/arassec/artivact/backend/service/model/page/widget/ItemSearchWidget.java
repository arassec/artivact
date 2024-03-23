package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.WidgetType;

/**
 * Displays search input and the result items of the search in a grid.
 */
public class ItemSearchWidget extends SearchBasedWidget {

    /**
     * Creates a new instance.
     */
    protected ItemSearchWidget() {
        super(WidgetType.ITEM_SEARCH);
    }

}
