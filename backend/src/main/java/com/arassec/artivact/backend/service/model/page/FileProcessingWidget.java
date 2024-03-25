package com.arassec.artivact.backend.service.model.page;

import java.util.List;

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

    /**
     * Returns all files used by the widget.
     *
     * @return List of filenames (without path).
     */
    List<String> usedFiles();

}
