package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Displays search input and the result items of the search in a grid.
 */
@Getter
@Setter
public class ItemSearchWidget extends SearchBasedWidget {

    /**
     * Size of the search result page.
     */
    private int pageSize;

    /**
     * Creates a new instance.
     */
    protected ItemSearchWidget() {
        super(WidgetType.ITEM_SEARCH);
    }

}
