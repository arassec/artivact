package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.IdentifiedObject;
import lombok.Data;

/**
 * A page of the application.
 */
@Data
public class Page implements IdentifiedObject {

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

    /**
     * The page's 'work-in-progress' content.
     */
    private PageContent wipPageContent;

    /**
     * An alias under which this page can be opened, instead of using its ID.
     */
    private String alias;

}
