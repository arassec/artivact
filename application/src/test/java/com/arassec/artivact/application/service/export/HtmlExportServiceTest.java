package com.arassec.artivact.application.service.export;

import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.exchange.CollectionExport;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HtmlExportServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JsonMapper jsonMapper;

    private HtmlExportService service;

    private final Path exportDir = Path.of("exports/test.artivact.collection");

    @BeforeEach
    void setup() {
        service = new HtmlExportService(fileRepository, templateEngine, jsonMapper);
    }

    @Test
    void testExportHtmlGeneratesHomePageWithoutLocales() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Test Title"));
        collectionExport.setDescription(new TranslatableString("Test Description"));

        Menu menu = new Menu();
        menu.setId("main");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(templateEngine.process(eq("export/home"), any(Context.class))).thenReturn("<html>home</html>");

        service.exportHtml(ctx, collectionExport, menu, List.of());

        verify(templateEngine).process(eq("export/home"), any(Context.class));
        verify(fileRepository).write(eq(exportDir.resolve("index.html")), any(byte[].class));
    }

    @Test
    void testExportHtmlGeneratesPagesForMultipleLocales() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu menu = new Menu();
        menu.setId("main");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(templateEngine.process(eq("export/home"), any(Context.class))).thenReturn("<html>home</html>");

        service.exportHtml(ctx, collectionExport, menu, List.of("en", "de"));

        verify(fileRepository).createDirIfRequired(exportDir.resolve("en"));
        verify(fileRepository).createDirIfRequired(exportDir.resolve("de"));

        verify(fileRepository).write(eq(exportDir.resolve("en/index.html")), any(byte[].class));
        verify(fileRepository).write(eq(exportDir.resolve("de/index.html")), any(byte[].class));
    }

    @Test
    void testExportHtmlGeneratesMenuPages() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu child = new Menu();
        child.setId("child-menu");
        child.setValue("Child");
        child.setTargetPageId("page1");

        Menu menu = new Menu();
        menu.setId("main");
        menu.setMenuEntries(List.of(child));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(List.of());

        when(fileRepository.exists(exportDir.resolve("page1.artivact.page-content.json"))).thenReturn(true);
        when(fileRepository.read(exportDir.resolve("page1.artivact.page-content.json"))).thenReturn("{}");
        when(jsonMapper.readValue("{}", PageContent.class)).thenReturn(pageContent);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of());

        verify(templateEngine).process(eq("export/home"), any(Context.class));
        verify(templateEngine).process(eq("export/page"), any(Context.class));
        verify(fileRepository).write(eq(exportDir.resolve("page1.html")), any(byte[].class));
    }

    @Test
    void testExportHtmlSkipsHiddenMenuEntries() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu hidden = new Menu();
        hidden.setId("hidden-menu");
        hidden.setValue("Hidden");
        hidden.setHidden(true);
        hidden.setTargetPageId("hidden-page");

        Menu menu = new Menu();
        menu.setId("main");
        menu.setMenuEntries(List.of(hidden));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of());

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("export/home"), contextCaptor.capture());

        @SuppressWarnings("unchecked")
        List<HtmlExportService.NavigationItem> navItems =
                (List<HtmlExportService.NavigationItem>) contextCaptor.getValue().getVariable("navigationItems");
        assertThat(navItems).hasSize(1);
        assertThat(navItems.getFirst().label()).isEqualTo("Home");
    }

    @Test
    void testExportHtmlWithWidgets() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu child = new Menu();
        child.setId("child");
        child.setValue("Page");
        child.setTargetPageId("widgets-page");

        Menu menu = new Menu();
        menu.setId("main");
        menu.setMenuEntries(List.of(child));

        TextWidget textWidget = new TextWidget();
        textWidget.setId("text1");
        textWidget.setHeading(new TranslatableString("Heading"));
        textWidget.setContent(new TranslatableString("Content"));

        AvatarWidget avatarWidget = new AvatarWidget();
        avatarWidget.setId("avatar1");
        avatarWidget.setAvatarImage("avatar.png");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(List.of(textWidget, avatarWidget));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(fileRepository.exists(exportDir.resolve("widgets-page.artivact.page-content.json"))).thenReturn(true);
        when(fileRepository.read(exportDir.resolve("widgets-page.artivact.page-content.json"))).thenReturn("{}");
        when(jsonMapper.readValue("{}", PageContent.class)).thenReturn(pageContent);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of());

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("export/page"), contextCaptor.capture());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> widgets =
                (List<Map<String, Object>>) contextCaptor.getValue().getVariable("widgets");
        assertThat(widgets).hasSize(2);
        assertThat(widgets.getFirst().get("type")).isEqualTo("TEXT");
        assertThat(widgets.getFirst().get("heading")).isEqualTo("Heading");
        assertThat(widgets.get(1).get("type")).isEqualTo("AVATAR");
        assertThat(widgets.get(1).get("avatarImage")).isEqualTo("avatar.png");
    }

    @Test
    void testExportHtmlLocaleResolution() {
        CollectionExport collectionExport = new CollectionExport();
        TranslatableString title = new TranslatableString("English Title");
        title.setTranslations(Map.of("de", "German Title"));
        collectionExport.setTitle(title);

        Menu menu = new Menu();
        menu.setId("main");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(templateEngine.process(eq("export/home"), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of("en", "de"));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, times(2)).process(eq("export/home"), contextCaptor.capture());

        List<Context> contexts = contextCaptor.getAllValues();

        Context enContext = contexts.stream()
                .filter(c -> "en".equals(c.getVariable("currentLocale")))
                .findFirst().orElseThrow();
        assertThat(enContext.getVariable("pageTitle")).isEqualTo("English Title");

        Context deContext = contexts.stream()
                .filter(c -> "de".equals(c.getVariable("currentLocale")))
                .findFirst().orElseThrow();
        assertThat(deContext.getVariable("pageTitle")).isEqualTo("German Title");
    }

    @Test
    void testExportHtmlCssPathForLocalePages() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu menu = new Menu();
        menu.setId("main");

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(templateEngine.process(eq("export/home"), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of("en"));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("export/home"), contextCaptor.capture());

        assertThat(contextCaptor.getValue().getVariable("cssPath")).isEqualTo("../artivact-export.css");
        assertThat(contextCaptor.getValue().getVariable("localeBasePath")).isEqualTo("../");
    }

    @Test
    void testExportHtmlWithNestedMenus() {
        CollectionExport collectionExport = new CollectionExport();
        collectionExport.setTitle(new TranslatableString("Title"));

        Menu grandchild = new Menu();
        grandchild.setId("grandchild");
        grandchild.setValue("Grandchild");
        grandchild.setTargetPageId("gc-page");

        Menu child = new Menu();
        child.setId("child");
        child.setValue("Child");
        child.setMenuEntries(List.of(grandchild));

        Menu menu = new Menu();
        menu.setId("main");
        menu.setMenuEntries(List.of(child));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(List.of());

        when(fileRepository.exists(exportDir.resolve("gc-page.artivact.page-content.json"))).thenReturn(true);
        when(fileRepository.read(exportDir.resolve("gc-page.artivact.page-content.json"))).thenReturn("{}");
        when(jsonMapper.readValue("{}", PageContent.class)).thenReturn(pageContent);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        service.exportHtml(ctx, collectionExport, menu, List.of());

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("export/home"), contextCaptor.capture());

        @SuppressWarnings("unchecked")
        List<HtmlExportService.NavigationItem> navItems =
                (List<HtmlExportService.NavigationItem>) contextCaptor.getValue().getVariable("navigationItems");
        assertThat(navItems).hasSize(2);
        assertThat(navItems.get(1).label()).isEqualTo("Child");
        assertThat(navItems.get(1).children()).hasSize(1);
        assertThat(navItems.get(1).children().getFirst().label()).isEqualTo("Grandchild");
    }

}
