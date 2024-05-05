package com.arassec.artivact.backend.service.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Parameters for content export.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportParams {

    /**
     * The main export dir containing all exports.
     */
    @JsonIgnore
    private Path exportDir;

    /**
     * The export dir of the specific content export.
     */
    @JsonIgnore
    private Path contentExportDir;

    /**
     * The final export result, if the export should be zipped.
     */
    @JsonIgnore
    private Path contentExportFile;

    /**
     * The target export types.
     */
    @Builder.Default
    private ExportType exportType = ExportType.JSON;

    /**
     * Set to {@code true} to ZIP the resulting export.
     */
    @Builder.Default
    private boolean zipResults = false;

    /**
     * Set to {@code true} to filter restricted content from exports.
     */
    @Builder.Default
    private boolean applyRestrictions = false;

    /**
     * Set to {@code true} to export the minimal set of media files to reduce export size.
     */
    @Builder.Default
    private boolean optimizeSize = false;

}
