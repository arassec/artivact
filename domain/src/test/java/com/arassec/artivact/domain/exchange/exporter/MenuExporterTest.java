package com.arassec.artivact.domain.exchange.exporter;


import com.arassec.artivact.core.model.Roles;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.menu.Menu;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.arassec.artivact.domain.service.PageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link MenuExporter}.
 */
@ExtendWith(MockitoExtension.class)
class MenuExporterTest {

    /**
     * The exporter under test.
     */
    @InjectMocks
    private MenuExporter menuExporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Exporter for pages.
     */
    @Mock
    private PageExporter pageExporter;

    /**
     * The service for page handling.
     */
    @Mock
    private PageService pageService;

    /**
     * Tests exporting a single menu with a page.
     */
    @Test
    @SneakyThrows
    void testExportSingleMenuWithoutRestrictions() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(new ExportConfiguration());

        Menu menu = new Menu();
        menu.setId("123-abc");
        menu.setValue("title");
        menu.setTargetPageId("456-def");

        PageContent pageContent = new PageContent();
        when(pageService.loadPageContent("456-def", Set.of(Roles.ROLE_ADMIN, Roles.ROLE_USER)))
                .thenReturn(pageContent);

        menuExporter.exportMenu(exportContext, menu);

        verify(objectMapper).writeValue(Path.of("export-dir/123-abc.artivact.menu.json").toFile(), menu);

        verify(pageExporter).exportPage(exportContext, "456-def", pageContent);
    }

    /**
     * Tests exporting a menu with sub-menus and restrictions.
     */
    @Test
    @SneakyThrows
    void testExportMultiMenuWithRestrictions() {
        ExportContext exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder()
                .applyRestrictions(true)
                .build());

        Menu subMenuOne = new Menu();
        subMenuOne.setId("456-def");
        subMenuOne.setRestrictions(Set.of());
        Menu subMenuTwo = new Menu();
        subMenuTwo.setId("666-omg");
        subMenuTwo.setRestrictions(Set.of(Roles.ROLE_ADMIN));
        Menu subMenuThree = new Menu();
        subMenuThree.setId("789-xyz");
        subMenuThree.setRestrictions(Set.of());

        Menu menu = new Menu();
        menu.setId("123-abc");
        menu.setValue("title");
        menu.getMenuEntries().add(subMenuOne);
        menu.getMenuEntries().add(subMenuTwo); // Must be filtered!
        menu.getMenuEntries().add(subMenuThree);

        menuExporter.exportMenu(exportContext, menu);

        verify(objectMapper).writeValue(Path.of("export-dir/123-abc.artivact.menu.json").toFile(), menu);
        verify(objectMapper).writeValue(Path.of("export-dir/456-def.artivact.menu.json").toFile(), subMenuOne);
        verify(objectMapper).writeValue(Path.of("export-dir/789-xyz.artivact.menu.json").toFile(), subMenuThree);

        verify(objectMapper, times(0))
                .writeValue(Path.of("export-dir/666-omg.artivact.menu.json").toFile(), subMenuTwo);

        assertThat(menu.getMenuEntries()).hasSize(2);
    }

}
