package com.arassec.artivact.domain.model.item;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains all media-creation content of an item.
 */
@Data
@NoArgsConstructor
public class MediaCreationContent {

    /**
     * List of sets of images that are showing this item and can be used as input to create models.
     */
    private List<CreationImageSet> imageSets = new LinkedList<>();

    /**
     * List of models of this item that were created from image-sets.
     */
    private List<CreationModelSet> modelSets = new LinkedList<>();

    /**
     * Creates a new media-creation content instance.
     */
    public MediaCreationContent(List<CreationImageSet> imageSets, List<CreationModelSet> modelSets) {
        if (imageSets != null) {
            this.imageSets = imageSets;
        }
        if (modelSets != null) {
            this.modelSets = modelSets;
        }
    }

}
