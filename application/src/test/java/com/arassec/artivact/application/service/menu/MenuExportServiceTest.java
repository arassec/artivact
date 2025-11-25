package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.menu.LoadMenuUseCase;
import com.arassec.artivact.application.port.in.page.ExportPageUseCase;
import com.arassec.artivact.application.port.in.page.LoadPageContentUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuExportServiceTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadPageContentUseCase loadPageContentUseCase;

    @Mock
    private ExportPageUseCase exportPageUseCase;

    @Mock
    private LoadMenuUseCase loadMenuUseCase;

    @InjectMocks
    private MenuExportService service;

    private final Path exportsDir = Path.of("exports");

    @Test
    void testExportMenuById() {
        when(useProjectDirsUseCase.getExportsDir()).thenReturn(exportsDir);

        Menu menu = new Menu();
        menu.setId("main-menu");

        when(loadMenuUseCase.loadMenu("main-menu")).thenReturn(menu);

        Path result = service.exportMenu("main-menu");

        assertThat(result.toString()).endsWith("main-menu.artivact.collection.zip");
        verify(fileRepository).pack(any(), eq(result));
        verify(fileRepository).delete(any());
    }

    @Test
    @SneakyThrows
    void testExportMenuFiltersRestrictedEntries() {
        Menu menu = new Menu();
        menu.setId("submenu");

        Menu entryWithRestriction = new Menu();
        entryWithRestriction.setId("restricted");
        entryWithRestriction.setRestrictions(Set.of("ROLE_ADMIN"));

        Menu entryWithoutRestriction = new Menu();
        entryWithoutRestriction.setId("allowed");

        menu.setMenuEntries(List.of(entryWithRestriction, entryWithoutRestriction));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().applyRestrictions(true).build())
                .build();

        service.exportMenu(ctx, menu);

        assertThat(menu.getMenuEntries())
                .extracting(Menu::getId)
                .containsExactly("allowed");

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), eq(menu));
        assertThat(argCap.getValue().toString()).endsWith("submenu.artivact.menu.json");
    }

    @Test
    @SneakyThrows
    void testExportMenuWritesJsonAndRecursesIntoEntries() {
        Menu child = new Menu();
        child.setId("child");

        Menu parent = new Menu();
        parent.setId("parent");
        parent.setMenuEntries(List.of(child));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        service.exportMenu(ctx, parent);

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), eq(parent));
        assertThat(argCap.getValue().toString()).endsWith("parent.artivact.menu.json");

        verify(jsonMapper).writeValue(argCap.capture(), eq(child));
        assertThat(argCap.getValue().toString()).endsWith("child.artivact.menu.json");
    }

    @Test
    void testExportMenuWithTargetPageExportsPage() {
        Menu menu = new Menu();
        menu.setId("menuWithPage");
        menu.setTargetPageId("page1");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        PageContent pageContent = new PageContent();
        when(loadPageContentUseCase.loadPageContent(eq("page1"), anySet())).thenReturn(pageContent);

        service.exportMenu(ctx, menu);

        verify(exportPageUseCase).exportPage(ctx, "page1", pageContent);
    }

}
