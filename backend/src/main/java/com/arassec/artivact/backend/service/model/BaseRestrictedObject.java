package com.arassec.artivact.backend.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Base for restricted objects.
 */
@Getter
@Setter
public abstract class BaseRestrictedObject implements IdentifiedObject, RestrictedObject {

    /**
     * The object's ID.
     */
    private String id;

    /**
     * Restrictions that apply to the object, i.e. the roles allowed to work with this item.
     */
    private Set<String> restrictions = new HashSet<>();

}
