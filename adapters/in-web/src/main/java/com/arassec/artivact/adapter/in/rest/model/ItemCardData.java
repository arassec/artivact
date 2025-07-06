package com.arassec.artivact.adapter.in.rest.model;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.Builder;
import lombok.Data;

/**
 * Data for the application's "item card".
 */
@Data
@Builder
public class ItemCardData {

    /**
     * The item's ID.
     */
    private String itemId;

    /**
     * The item's title.
     */
    private TranslatableString title;

    /**
     * URL to the image used in the item's card.
     */
    private String imageUrl;

    /**
     * {@code true} if the item provides a 3D model.
     */
    private boolean hasModel;

}
