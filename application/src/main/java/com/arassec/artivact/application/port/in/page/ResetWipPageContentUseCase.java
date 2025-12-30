package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

/**
 * Use case for reset wip page content operations.
 */
public interface ResetWipPageContentUseCase {

    PageContent resetWipPageContent(String pageIdOrAlias);

}
