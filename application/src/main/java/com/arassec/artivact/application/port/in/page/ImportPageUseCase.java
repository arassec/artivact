package com.arassec.artivact.application.port.in.page;

import com.arassec.artivact.domain.model.exchange.ImportContext;

/**
 * Use case for import page operations.
 */
public interface ImportPageUseCase {

    /**
     * Imports a page.
     *
     * @param importContext The import context.
     * @param pageId        The page's ID.
     */
    void importPage(ImportContext importContext, String pageId, String pageAlias);

}
