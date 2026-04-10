package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for deleting content audio of a collection export.
 */
public interface DeleteCollectionExportContentAudioUseCase {

    /**
     * Deletes a content audio file from a collection export.
     *
     * @param id     The collection export's ID.
     * @param locale The locale of the audio to delete (empty string for default).
     */
    void deleteContentAudio(String id, String locale);

}
