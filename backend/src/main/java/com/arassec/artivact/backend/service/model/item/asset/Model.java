package com.arassec.artivact.backend.service.model.item.asset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * A 3D model associated to an item.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Model extends Asset {

    /**
     * A comment set during model creation. Just to help the user differentiate between different 3D models of an item.
     */
    private String comment;

}
