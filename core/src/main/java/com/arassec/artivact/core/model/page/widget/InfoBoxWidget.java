package com.arassec.artivact.core.model.page.widget;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Widget displaying a central info box with text and color.
 */
@Getter
@Setter
public class InfoBoxWidget extends Widget {

    /**
     * The box heading.
     */
    private TranslatableString heading;

    /**
     * The box content.
     */
    private TranslatableString content;

    /**
     * The box type, determining the color of the box.
     */
    private String boxType;

    /**
     * Creates a new instance.
     */
    public InfoBoxWidget() {
        super(WidgetType.INFO_BOX);
    }

}
