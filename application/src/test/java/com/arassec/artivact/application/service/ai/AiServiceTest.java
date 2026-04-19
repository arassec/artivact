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
import com.arassec.artivact.domain.model.page.Widget;
import com.arassec.artivact.domain.model.page.WidgetType;
import com.arassec.artivact.domain.model.page.widget.TextWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
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

    @Mock
    private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    @InjectMocks
    private AiService aiService;

    @Test
    void translateTextUsesProvidedLocale() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(eq(aiConfiguration), any())).thenReturn("Translated text");

        String result = aiService.translateText("Hello", "de");

        assertThat(result).isEqualTo("Translated text");
        verify(aiGateway).execute(eq(aiConfiguration), contains("Hello"));
    }

    @Test
    void translateTextUsesDefaultLocaleWhenLocaleIsNull() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setDefaultLocale("en");
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(eq(aiConfiguration), any())).thenReturn("Translated");

        String result = aiService.translateText("Hello", null);

        assertThat(result).isEqualTo("Translated");
    }

    @Test
    void translateTextThrowsExceptionForInvalidLocale() {
        assertThatThrownBy(() -> aiService.translateText("Hello", "zz"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");
    }

    @Test
    void translateTextIncludesPromptFromConfiguration() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationPrompt(new TranslatableString("Custom prompt"));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(eq(aiConfiguration), any())).thenReturn("Result");

        aiService.translateText("Hello", "de");

        verify(aiGateway).execute(aiConfiguration, "Custom prompt\n\nHello");
    }

    @Test
    void translateTextUsesEmptyPromptWhenTranslationPromptIsBlank() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        aiConfiguration.setTranslationPrompt(new TranslatableString(""));
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(aiGateway.execute(eq(aiConfiguration), any())).thenReturn("Result");

        aiService.translateText("Hello", "de");

        verify(aiGateway).execute(aiConfiguration, "\n\nHello");
    }

    @Test
    void convertToAudioThrowsExceptionForInvalidLocale() {
        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "../etc"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Invalid locale");
    }

    @Test
    void convertToAudioUsesDefaultLocaleWhenLocaleIsNull() {
        AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
        appearanceConfiguration.setDefaultLocale("en");
        when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

        Page page = createPageWithTextWidget("Some content");
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(fileRepository.getSubdirFilePath(any(), eq("widget-1"), eq("wip"))).thenReturn(Path.of("/tmp/widgets/widget-1/wip"));
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/tmp/widgets"));

        String result = aiService.convertToAudio("page-1", "widget-1", null);

        assertThat(result).isEqualTo("content-audio-en.mp3");
        verify(pageRepository).save(page);
    }

    @Test
    void convertToAudioThrowsExceptionWhenPageNotFound() {
        when(pageRepository.findByIdOrAlias("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiService.convertToAudio("missing", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Page not found");
    }

    @Test
    void convertToAudioThrowsExceptionWhenWidgetNotFound() {
        Page page = new Page();
        PageContent wipContent = new PageContent();
        wipContent.setWidgets(List.of());
        page.setWipPageContent(wipContent);
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Widget not found");
    }

    @Test
    void convertToAudioThrowsExceptionWhenWidgetDoesNotSupportAudio() {
        Widget nonAudioWidget = new Widget(WidgetType.ITEM_SEARCH) {
        };
        nonAudioWidget.setId("widget-1");

        Page page = new Page();
        PageContent wipContent = new PageContent();
        wipContent.setWidgets(List.of(nonAudioWidget));
        page.setWipPageContent(wipContent);
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Widget does not support content audio");
    }

    @Test
    void convertToAudioThrowsExceptionWhenContentIsEmpty() {
        Page page = createPageWithTextWidget("");
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        assertThatThrownBy(() -> aiService.convertToAudio("page-1", "widget-1", "de"))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("No content available for audio generation");
    }

    @Test
    void convertToAudioGeneratesAudioFileAndSavesPage() {
        Page page = createPageWithTextWidget("Some content");
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/tmp/widgets"));
        when(fileRepository.getSubdirFilePath(any(), eq("widget-1"), eq("wip"))).thenReturn(Path.of("/tmp/widgets/widget-1/wip"));

        String result = aiService.convertToAudio("page-1", "widget-1", "de");

        assertThat(result).isEqualTo("content-audio-de.mp3");
        verify(aiGateway).convertToAudio(eq(aiConfiguration), eq("Some content"), any(), eq(Path.of("/tmp/widgets/widget-1/wip/content-audio-de.mp3")));
        verify(fileRepository).createDirIfRequired(Path.of("/tmp/widgets/widget-1/wip"));
        verify(pageRepository).save(page);
    }

    @Test
    void convertToAudioUsesDefaultFilenameWhenLocaleIsEmpty() {
        Page page = createPageWithTextWidget("Some content");
        when(pageRepository.findByIdOrAlias("page-1")).thenReturn(Optional.of(page));

        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getWidgetsDir()).thenReturn(Path.of("/tmp/widgets"));
        when(fileRepository.getSubdirFilePath(any(), eq("widget-1"), eq("wip"))).thenReturn(Path.of("/tmp/widgets/widget-1/wip"));

        String result = aiService.convertToAudio("page-1", "widget-1", "");

        assertThat(result).isEqualTo("content-audio.mp3");
    }

    @Test
    void testTtsConvertsTextToAudioInTempDir() {
        AiConfiguration aiConfiguration = new AiConfiguration();
        when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("/tmp"));

        aiService.testTts("Hello world");

        verify(fileRepository).createDirIfRequired(Path.of("/tmp"));
        verify(aiGateway).convertToAudio(eq(aiConfiguration), eq("Hello world"), any(), eq(Path.of("/tmp/audio-content.mp3")));
    }

    @Test
    void loadTestTtsAudioReturnsAudioBytes() {
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(Path.of("/tmp/audio-content.mp3"))).thenReturn(true);
        when(fileRepository.readBytes(Path.of("/tmp/audio-content.mp3"))).thenReturn(new byte[]{1, 2, 3});

        byte[] result = aiService.loadTestTtsAudio();

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void loadTestTtsAudioThrowsExceptionWhenFileNotFound() {
        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("/tmp"));
        when(fileRepository.exists(Path.of("/tmp/audio-content.mp3"))).thenReturn(false);

        assertThatThrownBy(() -> aiService.loadTestTtsAudio())
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Test audio file not found");
    }

    private Page createPageWithTextWidget(String content) {
        TextWidget textWidget = new TextWidget();
        textWidget.setId("widget-1");
        textWidget.setContent(new TranslatableString(content));

        Page page = new Page();
        PageContent wipContent = new PageContent();
        wipContent.setWidgets(List.of(textWidget));
        page.setWipPageContent(wipContent);
        return page;
    }

}
