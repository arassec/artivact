package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarouselWidget extends Widget {

    private String searchTerm;

    private int maxResults;

    protected ItemCarouselWidget() {
        super(WidgetType.ITEM_CAROUSEL);
    }

}
