package com.arassec.artivact.core.model.item;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains all media content of an item.
 */
@Data
public class MediaContent {

    /**
     * Images of the item.
     */
    private List<String> images = new LinkedList<>();

    /**
     * Models of the item.
     */
    private List<String> models = new LinkedList<>();

}
