package com.arassec.artivact.vault.backend.service.model;

import lombok.Getter;

public enum ImageSize {

    ORIGINAL(-1),

    CARD(300),

    CAROUSEL(1024);

    @Getter
    private final int width;

    ImageSize(int width) {
        this.width = width;
    }

}
