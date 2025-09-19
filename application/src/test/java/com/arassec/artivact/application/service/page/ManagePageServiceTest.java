package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.ConfigurationRepository;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.MenuRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ConfigurationType;
import com.arassec.artivact.domain.model.configuration.MenuConfiguration;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.FileProcessingWidget;
import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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
    void testUpdatePageAlias() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(page));

        service.updatePageAlias("page-1", "alias-123");

        assertThat(page.getAlias()).isEqualTo("alias-123");
        verify(pageRepository).save(page);
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
        when(menuRepository.load()).thenReturn(new MenuConfiguration(List.of(menu)));
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration("appTitle", null, null, null, null, null, null)));

        PageContent content = service.loadPageContent("page-1", Set.of(Roles.ROLE_USER));
        assertThat(content.getRestrictions()).contains(Roles.ROLE_USER);
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

        when(menuRepository.load()).thenReturn(new MenuConfiguration(List.of(new Menu())));
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("widgets"));
        when(configurationRepository.findByType(ConfigurationType.APPEARANCE, AppearanceConfiguration.class))
                .thenReturn(Optional.of(new AppearanceConfiguration("appTitle", null, null, null, null, null, null)));

        Widget widget = mock(Widget.class);
        when(widget.getId()).thenReturn("widget1");
        pageContent.getWidgets().add(widget);
        page.setPageContent(pageContent);
        page.setWipPageContent(new PageContent());

        when(fileRepository.getSubdirFilePath(eq(Path.of("widgets")), eq("widget1"), isNull())).thenReturn(Path.of("widgets/widget1"));
        when(fileRepository.getSubdirFilePath(eq(Path.of("widgets")), eq("widget1"), eq("wip"))).thenReturn(Path.of("widgets/widget1/wip"));

        when(fileRepository.exists(Path.of("widgets/widget1"))).thenReturn(true);
        when(fileRepository.exists(Path.of("widgets/widget1/wip"))).thenReturn(false);

        PageContent content = service.loadTranslatedRestrictedWipPageContent("page-1", Set.of(Roles.ROLE_ADMIN));
        assertThat(content).isNotNull();
        verify(fileRepository).createDirIfRequired(Path.of("widgets/widget1/wip"));
    }

    @Test
    void testLoadFileDelegatesToFileRepository() {
        when(fileRepository.getSubdirFilePath(any(), anyString(), any())).thenReturn(Path.of("widgets"));
        when(fileRepository.readBytes(any())).thenReturn(new byte[]{1, 2, 3});
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("widgets"));
        byte[] data = service.loadFile("widget1", "file.txt", null, true);
        assertThat(data).containsExactly(1, 2, 3);
    }

}
