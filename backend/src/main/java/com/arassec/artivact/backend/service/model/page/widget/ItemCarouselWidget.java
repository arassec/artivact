package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.WidgetType;

/**
 * Displays a carousel with items in a row from a predefined item search.
 */
public class ItemCarouselWidget extends SearchBasedWidget {

    /**
     * Creates a new instance.
     */
    protected ItemCarouselWidget() {
        super(WidgetType.ITEM_CAROUSEL);
    }

}
