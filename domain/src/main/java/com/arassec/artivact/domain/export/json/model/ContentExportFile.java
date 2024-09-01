package com.arassec.artivact.domain.export.json.model;

import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.core.model.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * The export file containing the main information about the export.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentExportFile {

    /**
     * Schema version for versioning export formats.
     */
    @Builder.Default
    private int schemaVersion = 1;

    /**
     * The title of the content.
     */
    private TranslatableString title;

    /**
     * The description of the content.
     */
    private TranslatableString description;

    /**
     * The ID of the main menu, root of the exported content.
     */
    private String menuId;

    /**
     * The item properties.
     */
    @Builder.Default
    private List<PropertyCategory> propertyCategories = new LinkedList<>();

    /**
     * The available tags.
     */
    @Builder.Default
    private List<Tag> tags = new LinkedList<>();

}
