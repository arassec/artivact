package com.arassec.artivact.backend.service.model.item;

import lombok.Getter;

/**
 * Available image sizes.
 */
@Getter
public enum ImageSize {

    /**
     * The original image.
     */
    ORIGINAL(-1),

    /**
     * Size to display an image in an item card.
     */
    ITEM_CARD(300),

    /**
     * Size to display an image in a carousel widget.
     */
    CAROUSEL(1024),

    /**
     * Size for page title images.
     */
    PAGE_TITLE(1600);

    /**
     * The image's width.
     */
    private final int width;

    /**
     * Creates a new instance.
     *
     * @param width The width.
     */
    ImageSize(int width) {
        this.width = width;
    }

}
