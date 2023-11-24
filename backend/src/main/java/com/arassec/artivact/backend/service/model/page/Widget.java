package com.arassec.artivact.backend.service.model.page;

import com.arassec.artivact.backend.service.model.BaseRestrictedItem;
import lombok.Getter;

@Getter
public abstract class Widget extends BaseRestrictedItem {

    private final WidgetType type;

    protected Widget(WidgetType type) {
        this.type = type;
    }

}
