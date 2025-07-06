package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.page.PageContent;

public interface ExportPageUseCase {

    /**
     * Exports a page.
     *
     * @param exportContext Context of the content export.
     * @param pageContent   The page to export.
     */
    void exportPage(ExportContext exportContext, String targetPageId, PageContent pageContent);

}
