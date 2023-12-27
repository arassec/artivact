package com.arassec.artivact.backend.service.model.item;

import lombok.Getter;

@Getter
public enum ImageSize {

    ORIGINAL(-1),

    ITEM_CARD(300),

    CAROUSEL(1024),

    PAGE_TITLE(1600);

    private final int width;

    ImageSize(int width) {
        this.width = width;
    }

}
