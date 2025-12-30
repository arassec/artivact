package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

/**
 * Use case for publish wip page content operations.
 */
public interface PublishWipPageContentUseCase {

    PageContent publishWipPageContent(String pageIdOrAlias);

}
