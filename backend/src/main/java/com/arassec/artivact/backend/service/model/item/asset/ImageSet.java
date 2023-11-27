package com.arassec.artivact.backend.service.model.item.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * A set of {@link Image}s which belong together, e.g. a series of pictures captured using a turn-table rotation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageSet {

    /**
     * Indicates whether the image set should be used as input for model creation ({@code true}) or not ({@code false}).
     */
    private boolean modelInput;

    /**
     * Indicates whether the image's background has been automatically removed ({@code true}) or not ({@code false}).
     */
    private Boolean backgroundRemoved;

    /**
     * The images of this set.
     */
    @Builder.Default
    private List<Image> images = new LinkedList<>();

}
