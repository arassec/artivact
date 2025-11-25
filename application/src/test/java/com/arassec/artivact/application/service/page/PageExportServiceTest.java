package com.arassec.artivact.application.service.page;

import com.arassec.artivact.application.port.in.item.ExportItemUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.in.search.SearchItemsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.exchange.ExportConfiguration;
import com.arassec.artivact.domain.model.exchange.ExportContext;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.*;
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
class PageExportServiceTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ExportItemUseCase exportItemUseCase;

    @Mock
    private SearchItemsUseCase searchItemsUseCase;

    @InjectMocks
    private PageExportService service;

    private final Path exportsDir = Path.of("exports");

    @Test
    void testExportPageSkippedDueToRestrictions() {
        PageContent content = new PageContent();
        content.setRestrictions(Set.of("ROLE_ADMIN"));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().applyRestrictions(true).build())
                .build();

        service.exportPage(ctx, "page1", content);

        verify(fileRepository, never()).write(any(), any());
    }

    @Test
    @SneakyThrows
    void testExportPageWithVariousWidgets() {
        AvatarWidget avatar = new AvatarWidget();
        avatar.setId("avatar1");
        avatar.setAvatarImage("avatar.png");

        InfoBoxWidget infoBox = new InfoBoxWidget();
        infoBox.setId("info1");

        PageTitleWidget title = new PageTitleWidget();
        title.setId("title1");
        title.setBackgroundImage("bg.png");

        TextWidget text = new TextWidget();
        text.setId("text1");

        ImageGalleryWidget gallery = new ImageGalleryWidget();
        gallery.setId("gallery1");
        gallery.setImages(List.of("img1.png", "img2.png"));

        PageContent content = new PageContent();
        content.setWidgets(List.of(avatar, infoBox, title, text, gallery));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().build())
                .build();

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("project-root"));

        when(fileRepository.getDirFromId(any(Path.class), anyString())).thenReturn(Path.of("widgets"));

        service.exportPage(ctx, "page2", content);

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), eq(content));
        assertThat(argCap.getValue().toString()).endsWith("page2.artivact.page-content.json");
    }

    @Test
    @SneakyThrows
    void testExportPageSkipsRestrictedWidget() {
        AvatarWidget restricted = new AvatarWidget();
        restricted.setId("avatarX");
        restricted.setRestrictions(Set.of("ROLE_ADMIN"));

        PageContent content = new PageContent();
        content.setWidgets(List.of(restricted));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().applyRestrictions(true).build())
                .build();

        service.exportPage(ctx, "page3", content);

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), eq(content));
        assertThat(argCap.getValue().toString()).endsWith("page3.artivact.page-content.json");

        verify(fileRepository, never()).copy(any(), any());
    }

    @Test
    @SneakyThrows
    void testExportPageWithItemSearchWidget() {
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("search1");
        widget.setSearchTerm("test");
        widget.setMaxResults(5);

        PageContent content = new PageContent();
        content.setWidgets(List.of(widget));

        ExportContext ctx = ExportContext.builder()
                .exportDir(exportsDir)
                .exportConfiguration(ExportConfiguration.builder().excludeItems(false).build())
                .build();

        Item item1 = new Item();
        item1.setId("item1");

        Item item2 = new Item();
        item2.setId("item2");
        item2.setRestrictions(Set.of("ROLE_ADMIN")); // sollte ausgeschlossen werden

        when(searchItemsUseCase.search("test", 5)).thenReturn(List.of(item1, item2));

        service.exportPage(ctx, "page4", content);

        verify(exportItemUseCase).exportItem(ctx, item1);

        ArgumentCaptor<File> argCap = ArgumentCaptor.forClass(File.class);
        verify(jsonMapper).writeValue(argCap.capture(), eq(content));
        assertThat(argCap.getValue().toString()).endsWith("page4.artivact.page-content.json");
    }

}
