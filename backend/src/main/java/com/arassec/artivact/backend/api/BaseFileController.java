package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.service.model.item.Item;

public abstract class BaseFileController {

    protected String createMainImageUrl(Item item) {
        if (!item.getMediaContent().getImages().isEmpty()) {
            return createImageUrl(item.getId(), item.getMediaContent().getImages().get(0));
        }
        return null;
    }

    protected String createImageUrl(String itemId, String fileName) {
        return createUrl(itemId, fileName, "image");
    }

    protected String createModelUrl(String itemId, String fileName) {
        return createUrl(itemId, fileName, "model");
    }

    protected String createUrl(String itemId, String fileName, String fileType) {
        return "/api/item/" + itemId + "/" + fileType + "/" + fileName;
    }

}
