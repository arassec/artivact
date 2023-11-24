package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchWidget extends Widget {

    private String searchTerm;

    private int maxResults;

    protected SearchWidget() {
        super(WidgetType.SEARCH);
    }

}
