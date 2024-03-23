package com.arassec.artivact.backend.service.model.page;

import com.arassec.artivact.backend.service.model.page.widget.*;
import lombok.Getter;

import java.util.Arrays;

/**
 * The widget type.
 */
@Getter
public enum WidgetType {

    /**
     * A page title.
     */
    PAGE_TITLE(PageTitleWidget.class),

    /**
     * A text block.
     */
    TEXT(TextWidget.class),

    /**
     * Item search.
     */
    ITEM_SEARCH(ItemSearchWidget.class),

    /**
     * Item carousel.
     */
    ITEM_CAROUSEL(ItemCarouselWidget.class),

    /**
     * An info box.
     */
    INFO_BOX(InfoBoxWidget.class),

    /**
     * An avatar.
     */
    AVATAR(AvatarWidget.class),

    /**
     * Some space.
     */
    SPACE(SpaceWidget.class),

    /**
     * An image with associated text.
     */
    IMAGE_TEXT(ImageTextWidget.class);

    /**
     * The widget's java class.
     */
    private final Class<? extends Widget> widgetClass;

    /**
     * Creates a new instance.
     *
     * @param widgetClass The widget's java class.
     */
    WidgetType(Class<? extends Widget> widgetClass) {
        this.widgetClass = widgetClass;
    }

    /**
     * Returns the java class for a given type.
     *
     * @param type The type to get the class for.
     * @return The java class.
     */
    public static Class<?> getClassOfType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(type))
                .findFirst()
                .orElseThrow()
                .getWidgetClass();
    }

}
