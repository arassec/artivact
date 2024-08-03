package com.arassec.artivact.core.model.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An asset of any kind, e.g. an image or 3D model.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Asset {

    /**
     * The asset's filename.
     */
    private String fileName;

    /**
     * The URL to download the asset.
     */
    private String url;

    /**
     * Indicates whether the asset can be transferred from the item's media-creation section to the item's media section.
     */
    private boolean transferable;

}
