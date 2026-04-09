package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.ItemSearchWidget;
import com.arassec.artivact.domain.model.page.widget.TextWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link AiService}.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiGateway aiGateway;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private LoadAiConfigurationUseCase loadAiConfigurationUseCase;

    @InjectMocks
    private AiService aiService;

    /**
     * Tests that the service delegates translation to the AI gateway.
     */
    @Test
    void testTranslateText() {
        when(aiGateway.translate("Hello", "de")).thenReturn("Hallo");

        String result = aiService.translateText("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
    }

    /**
     * Tests audio generation for a widget with default locale.
     */
    @Test
    void testConvertToAudioDefaultLocale() {
        TextWidget textWidget = new TextWidget();
        textWidget.setId("widget-1");
        textWidget.setContent(new TranslatableString("Hello World", null, new HashMap<>()));
        textWidget.setContentAudio(new TranslatableString());

        PageContent wipContent = new PageContent();
        wipContent.setWidgets(new LinkedList<>(List.of(textWidget)));

        Page page = new Page();
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/widgets"));
        when(fileRepository.getSubdirFilePath(any(), eq("widget-1"), eq("wip"))).thenReturn(Path.of("/widgets/widget-1/wip"));

        AiConfiguration aiConfig = new AiConfiguration();
        aiConfig.setTtsPrompt("Generate audio");
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfig);

        String result = aiService.convertToAudio("page-1", "widget-1", "");

        assertThat(result).isEqualTo("content-audio.mp3");
        verify(aiGateway).convertToAudio(eq("Generate audio"), eq("Hello World"), eq(Path.of("/widgets/widget-1/wip/content-audio.mp3")));
        verify(pageRepository).save(page);
        assertThat(textWidget.getContentAudio().getValue()).isEqualTo("content-audio.mp3");
    }

    /**
     * Tests audio generation for a widget with a specific locale.
     */
    @Test
    void testConvertToAudioWithLocale() {
        ItemSearchWidget widget = new ItemSearchWidget();
        widget.setId("widget-2");
        HashMap<String, String> translations = new HashMap<>();
        translations.put("de", "Hallo Welt");
        widget.setContent(new TranslatableString("Hello World", null, translations));
        widget.setContentAudio(new TranslatableString());

        PageContent wipContent = new PageContent();
        wipContent.setWidgets(new LinkedList<>(List.of(widget)));

        Page page = new Page();
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/widgets"));
        when(fileRepository.getSubdirFilePath(any(), eq("widget-2"), eq("wip"))).thenReturn(Path.of("/widgets/widget-2/wip"));

        AiConfiguration aiConfig = new AiConfiguration();
        aiConfig.setTtsPrompt("Generate audio");
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfig);

        String result = aiService.convertToAudio("page-1", "widget-2", "de");

        assertThat(result).isEqualTo("content-audio-de.mp3");
        verify(aiGateway).convertToAudio(eq("Generate audio"), eq("Hallo Welt"), eq(Path.of("/widgets/widget-2/wip/content-audio-de.mp3")));
        verify(pageRepository).save(page);
        assertThat(widget.getContentAudio().getTranslations()).containsEntry("de", "content-audio-de.mp3");
    }

    /**
     * Tests that an exception is thrown when the page is not found.
     */
    @Test
    void testConvertToAudioPageNotFound() {
        when(pageRepository.findByIdOrAlias("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiService.convertToAudio("unknown", "widget-1", ""))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Page not found");
    }

    /**
     * Tests that an exception is thrown when the widget is not found.
     */
    @Test
    void testConvertToAudioWidgetNotFound() {
        PageContent wipContent = new PageContent();
        wipContent.setWidgets(new LinkedList<>());

        Page page = new Page();
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "unknown", ""))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Widget not found");
    }

    /**
     * Tests that an exception is thrown when widget content is empty.
     */
    @Test
    void testConvertToAudioEmptyContent() {
        TextWidget textWidget = new TextWidget();
        textWidget.setId("widget-1");
        textWidget.setContent(new TranslatableString("", null, new HashMap<>()));

        PageContent wipContent = new PageContent();
        wipContent.setWidgets(new LinkedList<>(List.of(textWidget)));

        Page page = new Page();
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", ""))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No content available");
    }

    /**
     * Tests that contentAudio is initialized when null.
     */
    @Test
    void testConvertToAudioInitializesContentAudio() {
        TextWidget textWidget = new TextWidget();
        textWidget.setId("widget-1");
        textWidget.setContent(new TranslatableString("Hello", null, new HashMap<>()));
        // contentAudio is null - should be initialized

        PageContent wipContent = new PageContent();
        wipContent.setWidgets(new LinkedList<>(List.of(textWidget)));

        Page page = new Page();
        page.setWipPageContent(wipContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/widgets"));
        when(fileRepository.getSubdirFilePath(any(), eq("widget-1"), eq("wip"))).thenReturn(Path.of("/widgets/widget-1/wip"));

        AiConfiguration aiConfig = new AiConfiguration();
        aiConfig.setTtsPrompt("Prompt");
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfig);

        String result = aiService.convertToAudio("page-1", "widget-1", "");

        assertThat(result).isEqualTo("content-audio.mp3");
        assertThat(textWidget.getContentAudio()).isNotNull();
        assertThat(textWidget.getContentAudio().getValue()).isEqualTo("content-audio.mp3");
    }

}
