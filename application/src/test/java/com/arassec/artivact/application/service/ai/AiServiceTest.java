package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadAppearanceConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.widget.AvatarWidget;
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

    @Mock
    private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    @InjectMocks
    private AiService aiService;

    @Test
    void translatesTextUsingConfiguredPromptAndLocale() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationPrompt("Translate to {locale}.");

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(aiConfiguration, "Translate to de.\n\nHello")).thenReturn("Hallo");

        String result = aiService.translateText("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
        verify(aiGateway).execute(aiConfiguration, "Translate to de.\n\nHello");
    }

    @Test
    void translatesTextWhenNoPromptIsConfigured() {
        AiConfiguration aiConfiguration = new AiConfiguration();

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(eq(aiConfiguration), anyString())).thenReturn("Hallo");

        String result = aiService.translateText("Hello", "de");

        assertThat(result).isEqualTo("Hallo");
        verify(aiGateway).execute(eq(aiConfiguration), anyString());
    }

    @Test
    void rejectsInvalidLocaleWhenTranslatingText() {
        assertThatThrownBy(() -> aiService.translateText("Hello", "de123"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Invalid locale: de123");

        verifyNoInteractions(loadAiConfigurationUseCase, aiGateway);
    }

    @Test
    void convertsTranslatedWidgetContentToAudioAndStoresTranslatedAudioFilename() {
        TextWidget widget = new TextWidget();
        widget.setId("widget-1");

        TranslatableString content = new TranslatableString();
        content.setValue("Hello");
        HashMap<String, String> translations = new HashMap<>();
        translations.put("de", "Hallo");
        content.setTranslations(translations);
        widget.setContent(content);

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new LinkedList<>(List.of(widget)));

        Page page = new Page();
        page.setWipPageContent(pageContent);

        AiConfiguration aiConfiguration = new AiConfiguration();
        Path widgetsDir = Path.of("widgets");
        Path widgetWipDir = Path.of("widgets", "widget-1", "wip");

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(widgetsDir);
        when(fileRepository.getSubdirFilePath(widgetsDir, "widget-1", "wip")).thenReturn(widgetWipDir);

        String result = aiService.convertToAudio("page-1", "widget-1", "de");

        assertThat(result).isEqualTo("content-audio-de.mp3");
        assertThat(widget.getContentAudio().getTranslations()).containsEntry("de", "content-audio-de.mp3");
        verify(fileRepository).createDirIfRequired(widgetWipDir);
        verify(aiGateway).convertToAudio(aiConfiguration, "Hallo", widgetWipDir.resolve("content-audio-de.mp3"));
        verify(pageRepository).save(page);
    }

    @Test
    void convertsDefaultWidgetContentToAudioAndStoresDefaultAudioFilename() {
        TextWidget widget = new TextWidget();
        widget.setId("widget-1");

        TranslatableString content = new TranslatableString();
        content.setValue("Hello");
        widget.setContent(content);

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new LinkedList<>(List.of(widget)));

        Page page = new Page();
        page.setWipPageContent(pageContent);

        AiConfiguration aiConfiguration = new AiConfiguration();
        Path widgetsDir = Path.of("widgets");
        Path widgetWipDir = Path.of("widgets", "widget-1", "wip");

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(widgetsDir);
        when(fileRepository.getSubdirFilePath(widgetsDir, "widget-1", "wip")).thenReturn(widgetWipDir);

        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setDefaultLocale("");

        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration())
                .thenReturn(appearanceConfiguration);

        String result = aiService.convertToAudio("page-1", "widget-1", null);

        assertThat(result).isEqualTo("content-audio.mp3");
        assertThat(widget.getContentAudio().getValue()).isEqualTo("content-audio.mp3");
        verify(aiGateway).convertToAudio(aiConfiguration, "Hello", widgetWipDir.resolve("content-audio.mp3"));
        verify(pageRepository).save(page);
    }

    @Test
    void rejectsInvalidLocaleWhenConvertingToAudio() {
        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de123"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Invalid locale: de123");

        verifyNoInteractions(pageRepository, aiGateway);
    }

    @Test
    void throwsWhenPageForAudioConversionDoesNotExist() {
        when(pageRepository.findByIdOrAlias("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiService.convertToAudio("missing", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Page not found: missing");
    }

    @Test
    void throwsWhenWidgetForAudioConversionDoesNotExist() {
        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new LinkedList<>());

        Page page = new Page();
        page.setWipPageContent(pageContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "missing", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Widget not found: missing");
    }

    @Test
    void throwsWhenWidgetDoesNotSupportContentAudio() {
        AvatarWidget widget = new AvatarWidget();
        widget.setId("widget-1");

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new LinkedList<>(List.of(widget)));

        Page page = new Page();
        page.setWipPageContent(pageContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Widget does not support content audio: widget-1");
    }

    @Test
    void throwsWhenNoContentIsAvailableForAudioGeneration() {
        TextWidget widget = new TextWidget();
        widget.setId("widget-1");

        TranslatableString content = new TranslatableString();
        content.setValue("   ");
        widget.setContent(content);

        PageContent pageContent = new PageContent();
        pageContent.setWidgets(new LinkedList<>(List.of(widget)));

        Page page = new Page();
        page.setWipPageContent(pageContent);

        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessage("No content available for audio generation in widget: widget-1");
    }

    @Test
    void createsTestAudioFileInTempDirectory() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        Path tempDir = Path.of("temp");

        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);

        aiService.testTts("Hello");

        verify(fileRepository).createDirIfRequired(tempDir);
        verify(aiGateway).convertToAudio(aiConfiguration, "Hello", tempDir.resolve("audio-content.mp3"));
    }

    @Test
    void loadsGeneratedTestAudioFile() {
        Path tempDir = Path.of("temp");
        Path audioFile = tempDir.resolve("audio-content.mp3");
        byte[] expected = new byte[]{1, 2, 3};

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        when(fileRepository.exists(audioFile)).thenReturn(true);
        when(fileRepository.readBytes(audioFile)).thenReturn(expected);

        byte[] result = aiService.loadTestTtsAudio();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void throwsWhenGeneratedTestAudioFileDoesNotExist() {
        Path tempDir = Path.of("temp");
        Path audioFile = tempDir.resolve("audio-content.mp3");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(tempDir);
        when(fileRepository.exists(audioFile)).thenReturn(false);

        assertThatThrownBy(() -> aiService.loadTestTtsAudio())
                .isInstanceOf(ArtivactException.class)
                .hasMessage("Test audio file not found.");
    }

}
