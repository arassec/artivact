package com.arassec.artivact.adapter.in.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for favorite item information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItemData {

    /**
     * The item's ID.
     */
    private String itemId;

    /**
     * The item's title.
     */
    private String title;

    /**
     * URL to the item's thumbnail image.
     */
    private String thumbnailUrl;

}
