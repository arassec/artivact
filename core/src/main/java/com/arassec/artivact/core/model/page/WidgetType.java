package com.arassec.artivact.core.model.page;

import lombok.Getter;

/**
 * The widget type.
 */
@Getter
public enum WidgetType {

    /**
     * A page title.
     */
    PAGE_TITLE,

    /**
     * A text block.
     */
    TEXT,

    /**
     * Item search.
     */
    ITEM_SEARCH,

    /**
     * An info box.
     */
    INFO_BOX,

    /**
     * An avatar.
     */
    AVATAR,

    /**
     * Some space.
     */
    SPACE,

    /**
     * An image with associated text.
     */
    IMAGE_TEXT,

    /**
     * An image gallery.
     */
    IMAGE_GALLERY,

}
