package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

/**
 * Use case for reset wip page content operations.
 */
public interface ResetWipPageContentUseCase {

    /**
     * Publishes the WIP content of a page.
     *
     * @param pageIdOrAlias The page's ID or alias.
     * @return The published page content.
     */
    PageContent resetWipPageContent(String pageIdOrAlias);

}
