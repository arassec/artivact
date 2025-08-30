package com.arassec.artivact.domain.model.page;

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
     * An image gallery.
     */
    IMAGE_GALLERY,

    /**
     * A grid with one or more configurable buttons.
     */
    BUTTONS

}
