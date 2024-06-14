package com.arassec.artivact.backend.service.model.tag;

import com.arassec.artivact.backend.service.model.BaseTranslatableRestrictedObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A tag that can be put on an item.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag extends BaseTranslatableRestrictedObject {

    /**
     * Optional HTTP-URL for the tag.
     */
    private String url;

    /**
     * Set to {@code true}, if this tag should automatically be applied to newly created items.
     */
    private boolean defaultTag;

}
