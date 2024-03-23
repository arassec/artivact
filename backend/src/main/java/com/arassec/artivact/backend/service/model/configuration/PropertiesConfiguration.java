package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.property.PropertyCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of item properties.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertiesConfiguration {

    /**
     * Properties in their respective category.
     */
    private List<PropertyCategory> categories = new LinkedList<>();

}
