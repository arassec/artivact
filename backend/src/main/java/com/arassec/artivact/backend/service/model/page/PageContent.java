package com.arassec.artivact.backend.service.model.page;

import com.arassec.artivact.backend.service.model.BaseRestrictedObject;
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
     * If {@code true}, this page is considered to be the index page.
     */
    private Boolean indexPage;

    /**
     * Widgets of the page.
     */
    private List<Widget> widgets = new LinkedList<>();

}
