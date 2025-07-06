package com.arassec.artivact.domain.model.tag;

import com.arassec.artivact.domain.model.BaseTranslatableRestrictedObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A tag that can be put on an item.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
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
