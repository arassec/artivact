package com.arassec.artivact.core.model.page;

import java.util.List;

/**
 * Widget that is capable of handling files.
 */
public interface FileProcessingWidget {

    /**
     * Processes the file with the given name.
     *
     * @param filename  The file's name.
     * @param operation The operation to perform with the file.
     */
    void processFile(String filename, FileProcessingOperation operation);

    /**
     * Returns all files used by the widget.
     *
     * @return List of filenames (without path).
     */
    List<String> usedFiles();

}
