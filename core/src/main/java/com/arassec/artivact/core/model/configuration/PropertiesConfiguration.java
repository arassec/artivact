package com.arassec.artivact.core.model.configuration;

import com.arassec.artivact.core.model.property.PropertyCategory;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of item properties.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertiesConfiguration {

    /**
     * Properties in their respective category.
     */
    @Builder.Default
    private List<PropertyCategory> categories = new LinkedList<>();

}
