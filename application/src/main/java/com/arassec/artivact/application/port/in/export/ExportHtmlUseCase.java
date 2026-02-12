package com.arassec.artivact.application.port.in.export;

import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;

import java.util.List;

/**
 * Use case for generating static HTML pages during export.
 */
public interface ExportHtmlUseCase {

    /**
     * Generates static HTML pages for the given export context.
     *
     * @param exportContext    The export context.
     * @param collectionExport The collection export metadata.
     * @param menu             The menu structure to export.
     * @param availableLocales The configured locales.
     */
    void exportHtml(ExportContext exportContext, CollectionExport collectionExport, Menu menu, List<String> availableLocales);

}
