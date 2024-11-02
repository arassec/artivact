package com.arassec.artivact.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Base for restricted and translatable objects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseTranslatableRestrictedObject extends TranslatableString implements IdentifiedObject, RestrictedObject {

    /**
     * The object's ID.
     */
    private String id;

    /**
     * Restrictions that apply to the object.
     */
    @Builder.Default
    private Set<String> restrictions = new HashSet<>();

}
