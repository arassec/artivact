package com.arassec.artivact.domain.exchange.exp;

import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.core.model.page.widget.PageTitleWidget;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Set;

import static com.arassec.artivact.core.model.Roles.ROLE_ADMIN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link PageExporter}.
 */
@ExtendWith(MockitoExtension.class)
class PageExporterTest {

    /**
     * Exporter under test.
     */
    @InjectMocks
    private PageExporter pageExporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Exporter for widgets.
     */
    @Mock
    private WidgetExporter widgetExporter;

    /**
     * Tests exporting page content.
     */
    @Test
    @SneakyThrows
    void testExport() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder().build());

        Widget widget = new PageTitleWidget();

        PageContent pageContent = new PageContent();
        pageContent.getWidgets().add(widget);

        pageExporter.exportPage(exportContext, "123-abc", pageContent);

        verify(widgetExporter).exportWidget(exportContext, widget);

        verify(objectMapper).writeValue(Path.of("export-dir/123-abc.artivact.page-content.json").toFile(), pageContent);
    }

    /**
     * Tests exporting page content with restricted widgets.
     */
    @Test
    @SneakyThrows
    void testExportWithRestrictedWidget() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder()
                .applyRestrictions(true)
                .build());

        Widget widget = new PageTitleWidget();
        Widget restrictedWidget = new ItemSearchWidget();
        restrictedWidget.setRestrictions(Set.of(ROLE_ADMIN));

        PageContent pageContent = new PageContent();
        pageContent.getWidgets().add(widget);
        pageContent.getWidgets().add(restrictedWidget);

        pageExporter.exportPage(exportContext, "123-abc", pageContent);

        verify(widgetExporter).exportWidget(exportContext, widget);
        verify(widgetExporter, times(0)).exportWidget(exportContext, restrictedWidget);

        verify(objectMapper).writeValue(Path.of("export-dir/123-abc.artivact.page-content.json").toFile(), pageContent);
    }

    /**
     * Tests exporting restricted page content.
     */
    @Test
    @SneakyThrows
    void testExportRestrictedPage() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder()
                .applyRestrictions(true)
                .build());

        PageContent pageContent = new PageContent();
        pageContent.setRestrictions(Set.of(ROLE_ADMIN));

        pageExporter.exportPage(exportContext, "123-abc", pageContent);

        verify(objectMapper, times(0))
                .writeValue(Path.of("export-dir/123-abc.artivact.page-content.json").toFile(), pageContent);
    }

}
