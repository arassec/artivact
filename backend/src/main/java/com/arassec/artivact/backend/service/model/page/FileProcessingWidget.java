package com.arassec.artivact.backend.service.model.page;

/**
 * Widget that is capable of handling files.
 */
public interface FileProcessingWidget {

    /**
     * Processes the file with the given name.
     *
     * @param filename The file's name.
     */
    void processFile(String filename);

}
