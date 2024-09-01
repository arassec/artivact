package com.arassec.artivact.core.model.export;

import com.arassec.artivact.core.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains details about a content export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentExport {

    /**
     * The export's ID.
     */
    private String id;

    /**
     * The export's title.
     */
    private TranslatableString title;

    /**
     * The type of export.
     */
    private String exportType;

    /**
     * Indicates whether the export is zipped or not.
     */
    private boolean zipped;

    /**
     * The export's description.
     */
    private TranslatableString description;

    /**
     * Timestamp of the last modification of the export.
     */
    private long lastModified;

    /**
     * The size of the export.
     */
    private long size;

}
