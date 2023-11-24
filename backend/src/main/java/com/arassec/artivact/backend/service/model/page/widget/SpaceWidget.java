package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpaceWidget extends Widget {

    private int size;

    public SpaceWidget() {
        super(WidgetType.SPACE);
    }

}
