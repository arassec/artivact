package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Base for search based widgets.
 */
@Getter
@Setter
public abstract class SearchBasedWidget extends Widget {

    /**
     * The search term to perform.
     */
    protected String searchTerm;

    /**
     * Maximum number of results.
     */
    protected int maxResults;

    /**
     * Creates a new instance.
     *
     * @param type The widget type.
     */
    protected SearchBasedWidget(WidgetType type) {
        super(type);
    }

}
