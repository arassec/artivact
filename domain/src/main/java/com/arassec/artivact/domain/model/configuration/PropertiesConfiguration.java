package com.arassec.artivact.domain.model.configuration;

import com.arassec.artivact.domain.model.property.PropertyCategory;
import lombok.Builder;
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
@Builder
public class PropertiesConfiguration {

    /**
     * Properties in their respective category.
     */
    @Builder.Default
    private List<PropertyCategory> categories = new LinkedList<>();

    /**
     * Creates a new properties configuration with the specified categories.
     */
    public PropertiesConfiguration(List<PropertyCategory> categories) {
        if (categories != null) {
            this.categories = categories;
        }
    }

}
