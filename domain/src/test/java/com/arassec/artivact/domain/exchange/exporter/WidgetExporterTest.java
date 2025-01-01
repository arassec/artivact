package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.model.TranslatableString;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.model.page.Widget;
import com.arassec.artivact.core.model.page.WidgetType;
import com.arassec.artivact.core.model.page.widget.*;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.arassec.artivact.domain.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link WidgetExporter}.
 */
@ExtendWith(MockitoExtension.class)
class WidgetExporterTest {

    /**
     * The exporter under test.
     */
    @InjectMocks
    private WidgetExporter exporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * The service for searching items.
     */
    @Mock
    private SearchService searchService;

    /**
     * The application's file repository.
     */
    @Mock
    private FileRepository fileRepository;

    /**
     * Exporter for items.
     */
    @Mock
    private ItemExporter itemExporter;

    /**
     * The application's project data provider.
     */
    @Mock
    private ProjectDataProvider projectDataProvider;

    /**
     * Export context for testing.
     */
    private final ExportContext exportContext = ExportContext.builder()
            .exportDir(Path.of("export-dir"))
            .exportConfiguration(ExportConfiguration.builder().build())
            .build();

    /**
     * Project root for testing.
     */
    private final Path projectRoot = Path.of("root");

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        lenient().when(projectDataProvider.getProjectRoot()).thenReturn(projectRoot);
    }

    /**
     * A test to assert that all widgets are tested for export.
     */
    @Test
    void testWidgetExportCoverage() {
        // If new widgets are added, make sure to test their exportability!
        assertThat(WidgetType.values()).hasSize(7);
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportAvatarWidget() {
        AvatarWidget widget = new AvatarWidget();
        widget.setId("widget-id");
        widget.setAvatarImage("image.jpg");

        Path sourceDir = projectRoot.resolve(widget.getId());
        when(fileRepository.getDirFromId(projectRoot.resolve("widgets"), widget.getId())).thenReturn(sourceDir);
        Path targetDir = exportContext.getExportDir().resolve(widget.getId());

        exporter.exportWidget(exportContext, widget);

        verify(fileRepository).copy(sourceDir.resolve("image.jpg"), targetDir.resolve("image.jpg"));
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportImageTextWidget() {
        ImageTextWidget widget = new ImageTextWidget();
        widget.setId("widget-id");
        widget.setImage("image.jpg");

        Path sourceDir = projectRoot.resolve(widget.getId());
        when(fileRepository.getDirFromId(projectRoot.resolve("widgets"), widget.getId())).thenReturn(sourceDir);
        Path targetDir = exportContext.getExportDir().resolve(widget.getId());

        exporter.exportWidget(exportContext, widget);

        verify(fileRepository).copy(sourceDir.resolve("image.jpg"), targetDir.resolve("image.jpg"));
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportInfoBoxWidget() {
        InfoBoxWidget widget = new InfoBoxWidget();
        widget.setId("widget-id");

        assertDoesNotThrow(() -> exporter.exportWidget(exportContext, widget));
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportPageTitleWidget() {
        PageTitleWidget widget = new PageTitleWidget();
        widget.setId("widget-id");
        widget.setBackgroundImage("image.jpg");

        Path sourceDir = projectRoot.resolve(widget.getId());
        when(fileRepository.getDirFromId(projectRoot.resolve("widgets"), widget.getId())).thenReturn(sourceDir);
        Path targetDir = exportContext.getExportDir().resolve(widget.getId());

        exporter.exportWidget(exportContext, widget);

        verify(fileRepository).copy(sourceDir.resolve("image.jpg"), targetDir.resolve("image.jpg"));
    }

    /**
     * Tests exporting an item search widget without items.
     */
    @Test
    void testExportItemSearchWidgetItemsExcluded() {
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("widget-id");
        widget.setSearchTerm("search-term");
        widget.setMaxResults(3);

        exportContext.getExportConfiguration().setExcludeItems(true);

        exporter.exportWidget(exportContext, widget);

        verify(searchService, times(0)).search("search-term", 3);
        verify(itemExporter, times(0)).exportItem(any(), any());
    }

    /**
     * Tests exporting an item search widget with items.
     */
    @Test
    @SneakyThrows
    void testExportItemSearchWidgetItemsIncluded() {
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("widget-id");
        widget.setSearchTerm("search-term");
        widget.setMaxResults(3);

        exportContext.getExportConfiguration().setExcludeItems(false);
        exportContext.getExportConfiguration().setApplyRestrictions(true);

        Item item = new Item();
        item.setId("item-one");
        Item restrictedItem = new Item();
        restrictedItem.setId("item-two");
        restrictedItem.setRestrictions(Set.of("admin"));

        when(searchService.search("search-term", 3)).thenReturn(List.of(item, restrictedItem));

        exporter.exportWidget(exportContext, widget);

        verify(itemExporter).exportItem(exportContext, item);
        verify(objectMapper).writeValue(Path.of("export-dir/widget-id.artivact.search-result.json").toFile(), new String[]{"item-one"});
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportTextWidget() {
        TextWidget widget = new TextWidget();
        widget.setId("widget-id");

        assertDoesNotThrow(() -> exporter.exportWidget(exportContext, widget));
    }

    /**
     * Test exporting a certain widget.
     */
    @Test
    void testExportSpaceWidget() {
        TranslatableString navigationTitle = new TranslatableString("navigation-title");
        navigationTitle.setTranslations(Map.of("de", "navigations-titel"));
        navigationTitle.translate(Locale.GERMAN);

        SpaceWidget widget = new SpaceWidget();
        widget.setId("widget-id");
        widget.setNavigationTitle(navigationTitle);

        assertThat(navigationTitle.getTranslatedValue()).isEqualTo("navigations-titel");

        assertDoesNotThrow(() -> exporter.exportWidget(exportContext, widget));

        assertThat(navigationTitle.getTranslatedValue()).isNull();
    }

    /**
     * Tests that exporting an unknown widget type fails.
     */
    @Test
    void testExportUnknownWidget() {
        TestWidget widget = new TestWidget();
        assertThrows(ArtivactException.class, () -> exporter.exportWidget(exportContext, widget));
    }

    /**
     * Widget for testing.
     */
    private static class TestWidget extends Widget {

        /**
         * Creates a new instance.
         */
        protected TestWidget() {
            super(WidgetType.SPACE);
        }
    }

}
