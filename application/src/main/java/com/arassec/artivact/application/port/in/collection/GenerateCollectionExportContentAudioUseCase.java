package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for generating content audio via AI for a collection export.
 */
public interface GenerateCollectionExportContentAudioUseCase {

    /**
     * Generates an audio file from the collection export's content using AI.
     *
     * @param id     The collection export's ID.
     * @param locale The locale for the audio generation (empty string for default).
     * @return The generated audio filename.
     */
    String generateContentAudio(String id, String locale);

}
