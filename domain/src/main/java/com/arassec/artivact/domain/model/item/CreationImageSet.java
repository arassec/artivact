package com.arassec.artivact.domain.model.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * A set of images which belong together, e.g. a series of pictures captured using a turn-table rotation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreationImageSet {

    /**
     * Indicates whether the image set should be used as input for model creation ({@code true}) or not ({@code false}).
     */
    private boolean modelInput;

    /**
     * Indicates whether the image's background has been automatically removed ({@code true}) or not ({@code false}).
     */
    private Boolean backgroundRemoved;

    /**
     * The filenames of the images of this set.
     */
    @Builder.Default
    private List<String> files = new LinkedList<>();

}
