package com.arassec.artivact.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Base for restricted objects.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseRestrictedObject implements IdentifiedObject, RestrictedObject {

    /**
     * The object's ID.
     */
    private String id;

    /**
     * Restrictions that apply to the object, i.e. the roles allowed to work with this item.
     */
    @Builder.Default
    private Set<String> restrictions = new HashSet<>();

}
