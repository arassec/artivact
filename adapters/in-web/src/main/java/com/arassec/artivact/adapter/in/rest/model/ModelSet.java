package com.arassec.artivact.adapter.in.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combines different files related to a single 3D model of an item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelSet {

    /**
     * Directory containing the files.
     */
    protected String directory;

    /**
     * A comment set during model creation. Just to help the user differentiate between different 3D models of an item.
     */
    private String comment;

    /**
     * The image to display for this model set.
     */
    private String modelSetImage;

}
