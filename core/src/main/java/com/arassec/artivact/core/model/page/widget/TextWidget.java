package com.arassec.artivact.core.model.page.widget;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Displays a central text with an optional heading.
 */
@Getter
@Setter
public class TextWidget extends Widget {

    /**
     * The heading.
     */
    private TranslatableString heading;

    /**
     * The text content.
     */
    private TranslatableString content;

    /**
     * Creates a new widget.
     */
    public TextWidget() {
        super(WidgetType.TEXT);
    }

}
