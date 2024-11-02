package com.arassec.artivact.core.model.property;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * A property defining an item.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Property extends BaseTranslatableRestrictedObject {

    /**
     * A possible value range of predefined property values.
     */
    @Builder.Default
    private List<BaseTranslatableRestrictedObject> valueRange = new LinkedList<>();

}
