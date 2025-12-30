package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

/**
 * Use case for publish wip page content operations.
 */
public interface PublishWipPageContentUseCase {

    /**
     * Publishes the WIP content of a page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The published page content.
     */
    PageContent publishWipPageContent(String pageIdOrAlias);

}
