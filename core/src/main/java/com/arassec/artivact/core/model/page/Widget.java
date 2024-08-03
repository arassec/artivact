package com.arassec.artivact.core.model.page;

import com.arassec.artivact.core.model.BaseRestrictedObject;
import lombok.Getter;

/**
 * A building block of a web-page.
 */
@Getter
public abstract class Widget extends BaseRestrictedObject {

    /**
     * The widget's type.
     */
    private final WidgetType type;

    /**
     * Creates a new instance.
     *
     * @param type The type to use.
     */
    protected Widget(WidgetType type) {
        this.type = type;
    }

}
