package com.arassec.artivact.application.port.in.collection;

import java.io.InputStream;

/**
 * Use case for saving content audio of a collection export.
 */
public interface SaveCollectionExportContentAudioUseCase {

    /**
     * Saves the provided audio file for the collection export.
     *
     * @param id               The collection export's ID.
     * @param locale           The locale of the audio file (empty string for default).
     * @param originalFilename The audio file's original name.
     * @param inputStream      The input stream providing the audio data.
     */
    void saveContentAudio(String id, String locale, String originalFilename, InputStream inputStream);

}
