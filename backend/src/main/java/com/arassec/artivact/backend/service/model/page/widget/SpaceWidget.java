package com.arassec.artivact.backend.service.model.page.widget;

import com.arassec.artivact.backend.service.model.page.Widget;
import com.arassec.artivact.backend.service.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Creates some vertical space in the page.
 */
@Getter
@Setter
public class SpaceWidget extends Widget {

    /**
     * The amount of space to create.
     */
    private int size;

    /**
     * Creates a new instance.
     */
    public SpaceWidget() {
        super(WidgetType.SPACE);
    }

}
