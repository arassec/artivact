package com.arassec.artivact.domain.model.property;

import com.arassec.artivact.domain.model.BaseTranslatableRestrictedObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A category combining multiple properties into groups.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PropertyCategory extends BaseTranslatableRestrictedObject {

    /**
     * The properties.
     */
    @Builder.Default
    private List<Property> properties = new LinkedList<>();

    /**
     * Creates a new property category instance.
     */
    @SuppressWarnings("unused") // Used by Jackson.
    public PropertyCategory(String value, String translatedValue, Map<String, String> translations, String id, Set<String> restrictions, List<Property> properties) {
        super(value, translatedValue, translations, id, restrictions);
        if (properties != null) {
            this.properties = properties;
        }
    }

}
