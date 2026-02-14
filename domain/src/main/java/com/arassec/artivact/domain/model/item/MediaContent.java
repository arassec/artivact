package com.arassec.artivact.domain.model.item;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains all media content of an item.
 */
@Data
@NoArgsConstructor
public class MediaContent {

    /**
     * Images of the item.
     */
    private List<String> images = new LinkedList<>();

    /**
     * Models of the item.
     */
    private List<String> models = new LinkedList<>();

    /**
     * Creates a new media content instance.
     */
    @SuppressWarnings("unused") // Used by Jackson.
    public MediaContent(List<String> images, List<String> models) {
        if (images != null) {
            this.images = images;
        }
        if (models != null) {
            this.models = models;
        }
    }

}
