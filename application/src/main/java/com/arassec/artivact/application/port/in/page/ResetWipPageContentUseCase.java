package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.page.PageContent;

public interface ResetWipPageContentUseCase {

    PageContent resetWipPageContent(String pageIdOrAlias);

}
