package com.arassec.artivact.core.model.item;

import com.arassec.artivact.core.model.BaseRestrictedObject;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.tag.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An item.
 */
@Data
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

}
