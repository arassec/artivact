package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SearchBasedWidget extends Widget {

    protected String searchTerm;

    protected int maxResults;

    protected SearchBasedWidget(WidgetType type) {
        super(type);
    }

}
