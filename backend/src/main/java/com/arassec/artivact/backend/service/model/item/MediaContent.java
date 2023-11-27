package com.arassec.artivact.backend.service.model.item;

import com.arassec.artivact.backend.service.model.item.asset.ImageSet;
import com.arassec.artivact.backend.service.model.item.asset.Model;
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

    /**
     * List of sets of images that are showing this item and can be used as input to create models.
     */
    private List<ImageSet> imageSets = new LinkedList<>();

    /**
     * List of models of this item that were created from image-sets.
     */
    private List<Model> modelAssets = new LinkedList<>();

}
