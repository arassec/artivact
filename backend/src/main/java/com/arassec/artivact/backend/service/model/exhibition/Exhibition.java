package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.BaseRestrictedObject;
import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.property.PropertyCategory;
import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * An exhibition provides items and their data and media files.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Exhibition extends BaseRestrictedObject {

    /**
     * Technical version.
     */
    private Integer version;

    /**
     * The exhibition's title.
     */
    private TranslatableString title;

    /**
     * The exhibition's description.
     */
    private TranslatableString description;

    /**
     * Stores the last modification time.
     */
    private Instant lastModified;

    /**
     * Application menus that are used as source to generate this exhibition.
     */
    private List<String> referencedMenuIds = new LinkedList<>();

    /**
     * Topics of this exhibition.
     */
    private List<Topic> topics = new LinkedList<>();

    /**
     * The item properties.
     */
    private List<PropertyCategory> propertyCategories = new LinkedList<>();

    /**
     * The available tags.
     */
    private List<Tag> tags = new LinkedList<>();

}
