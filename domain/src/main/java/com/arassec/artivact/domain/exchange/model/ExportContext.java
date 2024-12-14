package com.arassec.artivact.domain.exchange.model;

import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Context for an export.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportContext {

    /**
     * An optional ID of the export.
     */
    private String id;

    /**
     * The main export dir containing all exports.
     */
    private Path projectExportsDir;

    /**
     * The export dir of the specific content export.
     */
    private Path exportDir;

    /**
     * The final export result, if the export should be zipped.
     */
    private Path exportFile;

    /**
     * The export's configuration.
     */
    private ExportConfiguration exportConfiguration;

    /**
     * Optional file extension of a cover picture (if present). Only used for collection exports.
     */
    private String coverPictureExtension;

}
