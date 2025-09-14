package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.BaseRestrictedObject;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Content of a page.
 */
@Getter
@Setter
public class PageContent extends BaseRestrictedObject {

    /**
     * Widgets of the page.
     */
    private List<Widget> widgets = new LinkedList<>();

    /**
     * Indicates whether the current user can edit the page or not.
     */
    private boolean editable;

    /**
     * Metadata of a page.
     */
    private PageMetaData metaData;

}
