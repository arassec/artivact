package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.Page;

import java.util.Set;

public interface CreatePageUseCase {

    /**
     * Creates a new page.
     *
     * @param restrictions Set of restrictions that should apply to the new page.
     * @return The newly created page.
     */
    Page createPage(Set<String> restrictions);

}
