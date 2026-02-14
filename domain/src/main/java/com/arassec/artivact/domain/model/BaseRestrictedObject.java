package com.arassec.artivact.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    /**
     * Creates a new base restricted object with the specified ID and restrictions.
     */
    protected BaseRestrictedObject(String id, Set<String> restrictions) {
        this.id = id;
        if (restrictions != null) {
            this.restrictions = restrictions;
        }
    }

}
