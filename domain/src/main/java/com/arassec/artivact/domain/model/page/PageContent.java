package com.arassec.artivact.domain.model.page;

import com.arassec.artivact.domain.model.BaseRestrictedObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Content of a page.
 */
@Getter
@Setter
@NoArgsConstructor
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

    public PageContent(String id, Set<String> restrictions, List<Widget> widgets, boolean editable, PageMetaData metaData) {
        super(id, restrictions);
        if (widgets != null) {
            this.widgets = widgets;
        }
        this.editable = editable;
        this.metaData = metaData;
    }

}
