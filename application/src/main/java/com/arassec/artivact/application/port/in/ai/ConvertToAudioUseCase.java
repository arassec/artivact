package com.arassec.artivact.application.port.in.ai;

/**
 * Use case for converting widget content to audio using artificial intelligence.
 */
public interface ConvertToAudioUseCase {

    /**
     * Generates an audio file for a widget's content.
     *
     * @param pageId   The page's ID.
     * @param widgetId The widget's ID.
     * @param locale   The locale for the audio generation (can be empty for the default locale).
     * @return The name of the generated audio file.
     */
    String convertToAudio(String pageId, String widgetId, String locale);

}
