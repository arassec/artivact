package com.arassec.artivact.core.model.property;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * A category combining multiple properties into groups.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PropertyCategory extends BaseTranslatableRestrictedObject {

    /**
     * The properties.
     */
    @Builder.Default
    private List<Property> properties = new LinkedList<>();

}
