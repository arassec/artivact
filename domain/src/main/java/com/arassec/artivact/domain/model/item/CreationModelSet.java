package com.arassec.artivact.domain.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * All files associated with a 3D model of an item.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CreationModelSet {

    /**
     * Directory containing the files.
     */
    protected String directory;

    /**
     * A comment set during model creation. Just to help the user differentiate between different 3D models of an item.
     */
    private String comment;

}
