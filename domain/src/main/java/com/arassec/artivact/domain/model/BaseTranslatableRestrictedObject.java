package com.arassec.artivact.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base for restricted and translatable objects.
 */
@Getter
@Setter
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

    /**
     * Creates a new base translatable restricted object with the specified parameters.
     */
    public BaseTranslatableRestrictedObject(String value, String translatedValue,
                                            Map<String, String> translations, String id, Set<String> restrictions) {
        super(value, translatedValue, translations);
        this.id = id;
        if (restrictions != null) {
            this.restrictions = restrictions;
        }
    }

}
