package com.arassec.artivact.backend.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Base for restricted and translatable objects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseTranslatableRestrictedObject extends TranslatableString implements IdentifiedObject, RestrictedObject {

    /**
     * The object's ID.
     */
    private String id;

    /**
     * Restrictions that apply to the object.
     */
    private Set<String> restrictions = new HashSet<>();

}
