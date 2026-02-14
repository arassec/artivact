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
 * A property defining an item.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Property extends BaseTranslatableRestrictedObject {

    /**
     * A possible value range of predefined property values.
     */
    @Builder.Default
    private List<BaseTranslatableRestrictedObject> valueRange = new LinkedList<>();

    public Property(String value, String translatedValue, Map<String, String> translations, String id, Set<String> restrictions, List<BaseTranslatableRestrictedObject> valueRange) {
        super(value, translatedValue, translations, id, restrictions);
        if (valueRange != null) {
            this.valueRange = valueRange;
        }
    }

}
