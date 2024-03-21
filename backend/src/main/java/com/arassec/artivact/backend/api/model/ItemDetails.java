package com.arassec.artivact.backend.api.model;

import com.arassec.artivact.backend.service.model.TranslatableString;
import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * The item's details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetails {

    /**
     * The item's ID.
     */
    private String id;

    /**
     * The technical version of the item as stored in the database.
     */
    private Integer version;

    /**
     * Restrictions that apply to the item.
     */
    private List<String> restrictions;

    /**
     * The item's title.
     */
    private TranslatableString title;

    /**
     * The item's description.
     */
    private TranslatableString description;

    /**
     * Images of the item.
     */
    private List<Asset> images;

    /**
     * 3D models of the item.
     */
    private List<Asset> models;

    /**
     * Image sets of the item used for media creation.
     */
    private List<ImageSet> creationImageSets;

    /**
     * Model sets of the item used for media creation.
     */
    private List<ModelSet> creationModelSets;

    /**
     * The item's properties.
     */
    private Map<String, String> properties;

    /**
     * The item's tags.
     */
    private List<Tag> tags;

}
