package com.arassec.artivact.domain.model.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains a page's ID and alias if available.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageIdAndAlias {

    /**
     * The page's ID.
     */
    private String id;

    /**
     * The page's alias, if set.
     */
    private String alias;

}
