package com.arassec.artivact.application.service.ai;

import com.arassec.artivact.application.port.in.ai.ConvertToAudioUseCase;
import com.arassec.artivact.application.port.in.ai.TestAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.ai.TranslateTextUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.LoadAppearanceConfigurationUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.AiGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.page.ContentAudioProvider;
import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.Widget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

/**
 * Service for AI-based operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements TranslateTextUseCase, ConvertToAudioUseCase, TestAiConfigurationUseCase {

    /**
     * The name of the test audio file.
     */
    private static final String TEST_AUDIO_FILENAME = "audio-content.mp3";

    /**
     * The AI gateway for interacting with the AI provider.
     */
    private final AiGateway aiGateway;

    /**
     * Repository for pages.
     */
    private final PageRepository pageRepository;

    /**
     * Repository for file operations.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for project directory handling.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Use case for loading AI configuration.
     */
    private final LoadAiConfigurationUseCase loadAiConfigurationUseCase;

    private final LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public String translateText(String text, String locale) {
        if (StringUtils.hasText(locale) && !locale.matches("[a-zA-Z_-]+")) {
            throw new ArtivactException("Invalid locale: " + locale);
        }

        if (locale == null) {
            locale = loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration().getDefaultLocale();
        }

        AiConfiguration aiConfiguration = loadAiConfigurationUseCase.loadAiConfiguration();
        String prompt = aiConfiguration.getTranslationPrompt();
        if (!StringUtils.hasText(prompt)) {
            prompt = "";
        }

        prompt = prompt.replace("{locale}", locale);

        String fullPrompt = prompt + "\n\n" + text;

        return aiGateway.execute(aiConfiguration, fullPrompt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertToAudio(String pageId, String widgetId, String locale) {

        if (StringUtils.hasText(locale) && !locale.matches("[a-zA-Z_-]+")) {
            throw new ArtivactException("Invalid locale: " + locale);
        }

        Page page = pageRepository.findByIdOrAlias(pageId)
                .orElseThrow(() -> new ArtivactException("Page not found: " + pageId));

        Widget widget = page.getWipPageContent().getWidgets().stream()
                .filter(w -> w.getId().equals(widgetId))
                .findFirst()
                .orElseThrow(() -> new ArtivactException("Widget not found: " + widgetId));

        if (!(widget instanceof ContentAudioProvider contentAudioProvider)) {
            throw new ArtivactException("Widget does not support content audio: " + widgetId);
        }

        TranslatableString content = contentAudioProvider.getContent();
        String textContent = resolveText(content, locale);

        if (!StringUtils.hasText(textContent)) {
            throw new ArtivactException("No content available for audio generation in widget: " + widgetId);
        }

        AiConfiguration aiConfiguration = loadAiConfigurationUseCase.loadAiConfiguration();

        String audioFilename = StringUtils.hasText(locale)
                ? "content-audio-" + locale + ".mp3"
                : "content-audio.mp3";

        Path widgetWipDir = fileRepository.getSubdirFilePath(useProjectDirsUseCase.getWidgetsDir(), widgetId, "wip");
        fileRepository.createDirIfRequired(widgetWipDir);
        Path targetFile = widgetWipDir.resolve(audioFilename);

        aiGateway.convertToAudio(aiConfiguration, textContent, targetFile);

        TranslatableString contentAudio = contentAudioProvider.getContentAudio();
        if (contentAudio == null) {
            contentAudio = new TranslatableString();
            contentAudioProvider.setContentAudio(contentAudio);
        }
        if (StringUtils.hasText(locale)) {
            contentAudio.getTranslations().put(locale, audioFilename);
        } else {
            contentAudio.setValue(audioFilename);
        }

        pageRepository.save(page);

        return audioFilename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testTts(String text, String locale) {
        AiConfiguration aiConfiguration = loadAiConfigurationUseCase.loadAiConfiguration();

        Path tempDir = useProjectDirsUseCase.getTempDir();
        fileRepository.createDirIfRequired(tempDir);
        Path targetFile = tempDir.resolve(TEST_AUDIO_FILENAME);

        aiGateway.convertToAudio(aiConfiguration, text, targetFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadTestTtsAudio() {
        Path audioFile = useProjectDirsUseCase.getTempDir().resolve(TEST_AUDIO_FILENAME);
        if (!fileRepository.exists(audioFile)) {
            throw new ArtivactException("Test audio file not found.");
        }
        return fileRepository.readBytes(audioFile);
    }

    /**
     * Resolves the text for the given locale from a translatable string.
     *
     * @param content The translatable string.
     * @param locale  The locale to resolve.
     * @return The resolved text.
     */
    private String resolveText(TranslatableString content, String locale) {
        if (content == null) {
            return null;
        }
        content.translate(locale);
        return content.getTranslatedValue();
    }

}
