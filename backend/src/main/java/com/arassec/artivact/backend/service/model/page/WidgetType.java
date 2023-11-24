package com.arassec.artivact.backend.service.model.page;

import com.arassec.artivact.backend.service.model.page.widget.*;
import lombok.Getter;

import java.util.Arrays;

public enum WidgetType {

    PAGE_TITLE(PageTitleWidget.class),

    TEXT(TextWidget.class),

    SEARCH(SearchWidget.class),

    ITEM_CAROUSEL(ItemCarouselWidget.class),

    INFO_BOX(InfoBoxWidget.class),

    AVATAR(AvatarWidget.class),

    SPACE(SpaceWidget.class),

    IMAGE_TEXT(ImageTextWidget.class);

    @Getter
    private final Class<? extends Widget> widgetClass;

    WidgetType(Class<? extends Widget> widgetClass) {
        this.widgetClass = widgetClass;
    }

    public static Class<?> getClassOfType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(type))
                .findFirst()
                .orElseThrow()
                .getWidgetClass();
    }

}
