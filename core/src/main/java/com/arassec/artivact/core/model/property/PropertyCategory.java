package com.arassec.artivact.core.model.property;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * A category combining multiple properties into groups.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCategory extends BaseTranslatableRestrictedObject {

    /**
     * The properties.
     */
    private List<Property> properties = new LinkedList<>();

}
