package com.arassec.artivact.core.model.page;

import lombok.Data;

/**
 * A page of the application.
 */
@Data
public class Page {

    /**
     * The page's ID.
     */
    private String id;

    /**
     * Technical version.
     */
    private Integer version;

    /**
     * The page's content.
     */
    private PageContent pageContent;

}
