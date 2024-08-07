package com.arassec.artivact.web.model;

import com.arassec.artivact.core.model.item.Asset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * A set of images, bundled for easier media creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * The filenames of the images of this set.
     */
    @Builder.Default
    private List<Asset> images = new LinkedList<>();

}
