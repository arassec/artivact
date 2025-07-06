package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.BaseRestrictedObject;
import com.arassec.artivact.domain.model.TranslatableString;
import lombok.Getter;
import lombok.Setter;

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
     * The title used in the navigation.
     */
    @Setter
    private TranslatableString navigationTitle;

    /**
     * Creates a new instance.
     *
     * @param type The type to use.
     */
    protected Widget(WidgetType type) {
        this.type = type;
    }

}
