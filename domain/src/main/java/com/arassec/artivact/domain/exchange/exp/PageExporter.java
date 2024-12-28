package com.arassec.artivact.domain.exchange.exp;

import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.arassec.artivact.domain.exchange.ExchangeDefinitions.PAGE_EXCHANGE_FILE_SUFFIX;

/**
 * Exporter for {@link Page}s.
 */
@Component
@RequiredArgsConstructor
public class PageExporter extends BaseExporter {

    /**
     * The application's object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Exporter for widgets.
     */
    private final WidgetExporter widgetExporter;

    /**
     * Exports a page.
     *
     * @param exportContext Context of the content export.
     * @param pageContent   The page to export.
     */
    public void exportPage(ExportContext exportContext, String targetPageId, PageContent pageContent) {
        if (exportContext.getExportConfiguration().isApplyRestrictions() && !pageContent.getRestrictions().isEmpty()) {
            return;
        }

        pageContent.getWidgets().forEach(widget -> {
            if (exportContext.getExportConfiguration().isApplyRestrictions() && !widget.getRestrictions().isEmpty()) {
                return;
            }
            widgetExporter.exportWidget(exportContext, widget);
        });

        writeJsonFile(exportContext.getExportDir().resolve(targetPageId + PAGE_EXCHANGE_FILE_SUFFIX), pageContent);
    }

}
