package com.arassec.artivact.application.port.in.collection;

/**
 * Use case for loading content audio of a collection export.
 */
public interface LoadCollectionExportContentAudioUseCase {

    /**
     * Loads a collection export's content audio file.
     *
     * @param id       The ID of the collection export.
     * @param filename The audio filename.
     * @return The audio as byte array.
     */
    byte[] loadContentAudio(String id, String filename);

}
