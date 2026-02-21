package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagePageServiceTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ManagePageService service;

    private Page page;

    private PageContent pageContent;

    @BeforeEach
    void setUp() {
        pageContent = new PageContent();
        pageContent.setWidgets(new ArrayList<>());

        page = new Page();
        page.setId("page-1");
        page.setPageContent(pageContent);
        page.setWipPageContent(pageContent);
    }

    @Test
    void testCreatePage() {
        Set<String> restrictions = Set.of(Roles.ROLE_ADMIN);
        Page newPage = service.createPage(restrictions);

        assertThat(newPage.getId()).isNotNull();
        assertThat(newPage.getPageContent().getRestrictions()).isEqualTo(restrictions);
        verify(pageRepository).save(any(Page.class));
    }

    @Test
    void testDeletePage() {
        Widget widget = mock(Widget.class);
        when(widget.getId()).thenReturn("widget-1");
        pageContent.getWidgets().add(widget);

        when(pageRepository.deleteById("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("widgets"));
        when(fileRepository.getDirFromId(any(), eq("widget-1"))).thenReturn(Path.of("widgets/widget-1"));

        service.deletePage("page-1");

        verify(fileRepository).deleteAndPruneEmptyParents(Path.of("widgets/widget-1"));
        verify(pageRepository).deleteById("page-1");
    }

    @Test
    void testUpdatePageAlias() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(page));

        service.updatePageAlias("page-1", "alias-123");

        assertThat(page.getAlias()).isEqualTo("alias-123");
        verify(pageRepository).save(page);
    }

    @Test
    void testLoadIndexPageIdAndAlias() {
        AppearanceConfiguration config = new AppearanceConfiguration();
        config.setIndexPageId("index-page-id");
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(config));

        Page indexPage = new Page();
        indexPage.setId("index-page-id");
        indexPage.setAlias("index-alias");
        when(pageRepository.findById("index-page-id")).thenReturn(Optional.of(indexPage));

        Optional<PageIdAndAlias> result = service.loadIndexPageIdAndAlias();

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("index-page-id");
        assertThat(result.get().getAlias()).isEqualTo("index-alias");
    }

    @Test
    void testLoadIndexPageIdAndAliasEmptyConfig() {
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration()));

        Optional<PageIdAndAlias> result = service.loadIndexPageIdAndAlias();

        assertThat(result).isEmpty();
    }

    @Test
    void testLoadPageContentThrowsExceptionIfMissing() {
        when(pageRepository.findByIdOrAlias("unknown")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.loadPageContent("unknown", Set.of()));
    }

    @Test
    void testLoadPageContentSetsRestrictions() {
        Menu menu = new Menu();
        menu.setTargetPageId("page-1");
        menu.setRestrictions(Set.of(Roles.ROLE_USER));
        when(menuRepository.load()).thenReturn(List.of(menu));
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration("appTitle", null, null, null, null, null, null)));

        PageContent content = service.loadPageContent("page-1", Set.of(Roles.ROLE_USER));
        assertThat(content.getRestrictions()).contains(Roles.ROLE_USER);
    }

    @Test
    void testLoadTranslatedRestrictedPageContent() {
        Menu menu = new Menu();
        menu.setTargetPageId("page-1");
        when(menuRepository.load()).thenReturn(List.of(menu));
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration("appTitle", null, null, null, null, null, null)));

        PageContent content = service.loadTranslatedRestrictedPageContent("page-1", Set.of());
        assertThat(content).isNotNull();
        verify(pageRepository).findByIdOrAlias("page-1");
    }

    @Test
    void testSavePageContentImport() {
        String newPageId = "new-page-id";
        when(pageRepository.findByIdOrAlias(newPageId)).thenReturn(Optional.empty());

        PageContent newContent = new PageContent();
        newContent.setId(newPageId);

        when(jsonMapper.writeValueAsString(newContent)).thenReturn("{}");
        when(jsonMapper.readValue("{}", PageContent.class)).thenReturn(new PageContent());

        Menu menu = new Menu();
        menu.setTargetPageId(newPageId);
        when(menuRepository.load()).thenReturn(List.of(menu));

        PageContent savedContent = service.savePageContent(newPageId, Set.of(Roles.ROLE_ADMIN), newContent);

        assertThat(savedContent).isNotNull();
        verify(pageRepository).save(any(Page.class));
    }

    @Test
    void testSavePageContentUpdate() {
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        Menu menu = new Menu();
        menu.setTargetPageId("page-1");
        when(menuRepository.load()).thenReturn(List.of(menu));

        PageContent newContent = new PageContent();
        Widget keptWidget = mock(Widget.class);
        when(keptWidget.getId()).thenReturn("widget-1");
        newContent.getWidgets().add(keptWidget);

        Widget existingWidget1 = mock(Widget.class);
        when(existingWidget1.getId()).thenReturn("widget-1");

        page.getWipPageContent().setWidgets(new ArrayList<>(List.of(existingWidget1)));
        page.getPageContent().setWidgets(new ArrayList<>(List.of(existingWidget1)));
        page.setVersion(1);

        service.savePageContent("page-1", Set.of(Roles.ROLE_ADMIN), newContent);

        verify(pageRepository).save(page);
    }

    @Test
    void testSavePageContentThrowsIfMenuMissing() {
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(menuRepository.load()).thenReturn(List.of());

        PageContent content = new PageContent();
        assertThrows(ArtivactException.class, () -> service.savePageContent("page-1", Set.of(), content));
    }

    @Test
    void testSavePageContentThrowsIfNotEditable() {
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        Menu menu = new Menu();
        menu.setTargetPageId("page-1");
        when(menuRepository.load()).thenReturn(List.of(menu));

        Widget restrictedWidget = mock(Widget.class);
        when(restrictedWidget.getRestrictions()).thenReturn(Set.of(Roles.ROLE_ADMIN));
        page.getWipPageContent().getWidgets().add(restrictedWidget);

        PageContent content = new PageContent();
        Set<String> roles = Set.of(Roles.ROLE_USER);

        assertThrows(ArtivactException.class, () -> service.savePageContent("page-1", roles, content));
    }

    @Test
    void testSaveFileAndDeleteFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(file.getInputStream()).thenReturn(System.in);
        Path widgetDir = Path.of("widgets/widget1/wip");
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("widgets"));
        when(fileRepository.getSubdirFilePath(any(), anyString(), any())).thenReturn(widgetDir);

        // Add a FileProcessingWidget to the page
        Widget widget = mock(Widget.class, withSettings().extraInterfaces(FileProcessingWidget.class));
        when(widget.getId()).thenReturn("widget1");
        pageContent.getWidgets().add(widget);
        page.setWipPageContent(pageContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        String filename = service.saveFile("page-1", "widget1", file);
        assertThat(filename).isEqualTo("file.txt");
        verify(pageRepository).save(page);

        // Delete file
        service.deleteFile("page-1", "widget1", "file.txt");
        verify(fileRepository, atLeastOnce()).delete(any(Path.class));
    }

    @Test
    void testResetAndPublishWipPageContent() {
        PageContent wipContent = new PageContent();
        Widget widget = mock(Widget.class, withSettings().extraInterfaces(FileProcessingWidget.class));
        widget.setId("widget1");
        wipContent.getWidgets().add(widget);
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("widgets"));

        PageContent resetContent = service.resetWipPageContent("page-1");
        assertThat(resetContent).isEqualTo(pageContent);
        verify(pageRepository, atLeastOnce()).save(page);

        PageContent publishedContent = service.publishWipPageContent("page-1");
        assertThat(publishedContent).isEqualTo(page.getWipPageContent());
        verify(pageRepository, atLeastOnce()).save(page);
    }

    @Test
    void testLoadTranslatedRestrictedWipPageContentInitializesWipDir() {

        when(menuRepository.load()).thenReturn(List.of(new Menu()));
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        Path widgetsPath = Path.of("widgets");
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(widgetsPath);
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration("appTitle", null, null, null, null, null, null)));

        Widget widget = mock(Widget.class);
        when(widget.getId()).thenReturn("widget1");
        pageContent.getWidgets().add(widget);
        page.setPageContent(pageContent);
        page.setWipPageContent(new PageContent());

        Path widgetPath = Path.of("widgets/widget1");
        when(fileRepository.getSubdirFilePath(eq(widgetsPath), eq("widget1"), isNull())).thenReturn(widgetPath);
        Path widgetWipPath = Path.of("widgets/widget1/wip");
        when(fileRepository.getSubdirFilePath(widgetsPath, "widget1", "wip")).thenReturn(widgetWipPath);

        when(fileRepository.exists(widgetPath)).thenReturn(true);
        when(fileRepository.exists(widgetWipPath)).thenReturn(false);

        PageContent content = service.loadTranslatedRestrictedWipPageContent("page-1", Set.of(Roles.ROLE_ADMIN));
        assertThat(content).isNotNull();
        verify(fileRepository).createDirIfRequired(widgetWipPath);
    }

    @Test
    void testLoadFileDelegatesToFileRepository() {
        Path widgetsPath = Path.of("widgets");
        when(fileRepository.getSubdirFilePath(any(), anyString(), any())).thenReturn(widgetsPath);
        when(fileRepository.readBytes(any())).thenReturn(new byte[]{1, 2, 3});
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(widgetsPath);
        byte[] data = service.loadFile("widget1", "file.txt", null, true);
        assertThat(data).containsExactly(1, 2, 3);
    }

}
