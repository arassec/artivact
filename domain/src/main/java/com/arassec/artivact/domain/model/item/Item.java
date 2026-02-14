package com.arassec.artivact.domain.model.item;

import com.arassec.artivact.domain.model.BaseRestrictedObject;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.tag.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * An item.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Item extends BaseRestrictedObject {

    /**
     * Technical version.
     */
    private Integer version;

    /**
     * Version of the last synchronization with a remote instance.
     */
    private Integer syncVersion;

    /**
     * The item's title.
     */
    private TranslatableString title;

    /**
     * The item's description.
     */
    private TranslatableString description;

    /**
     * The item's property values.
     */
    private Map<String, TranslatableString> properties = new HashMap<>();

    /**
     * Assigned tags.
     */
    private List<Tag> tags = new LinkedList<>();

    /**
     * Media content of the item.
     */
    private MediaContent mediaContent = new MediaContent();

    /**
     * Media-creation content of the item.
     */
    private MediaCreationContent mediaCreationContent = new MediaCreationContent();

    /**
     * Creates a new item.
     */
    @SuppressWarnings("java:S107") // This constructor is required as fallback for Jackson JSON deserialization.
    public Item(String id, Set<String> restrictions, Integer version, Integer syncVersion, TranslatableString title,
                TranslatableString description, Map<String, TranslatableString> properties, List<Tag> tags,
                MediaContent mediaContent, MediaCreationContent mediaCreationContent) {
        super(id, restrictions);
        this.version = version;
        this.syncVersion = syncVersion;
        this.title = title;
        this.description = description;
        if (properties != null) {
            this.properties = properties;
        }
        if (tags != null) {
            this.tags = tags;
        }
        if (mediaContent != null) {
            this.mediaContent = mediaContent;
        }
        if (mediaCreationContent != null) {
            this.mediaCreationContent = mediaCreationContent;
        }
    }

}
