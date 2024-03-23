package com.arassec.artivact.backend.service.model.item;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains all media-creation content of an item.
 */
@Data
public class MediaCreationContent {

    /**
     * List of sets of images that are showing this item and can be used as input to create models.
     */
    private List<CreationImageSet> imageSets = new LinkedList<>();

    /**
     * List of models of this item that were created from image-sets.
     */
    private List<CreationModelSet> modelSets = new LinkedList<>();

}
