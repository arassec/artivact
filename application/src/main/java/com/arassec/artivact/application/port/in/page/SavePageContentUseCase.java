package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

import java.util.Set;

public interface SavePageContentUseCase {

    /**
     * Saves a page's content.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @param roles         The available roles.
     * @param pageContent   The content to save.
     * @return The updated page content.
     */
    PageContent savePageContent(String pageIdOrAlias, Set<String> roles, PageContent pageContent);

}
