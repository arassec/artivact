package com.arassec.artivact.core.model.exchange;

import com.arassec.artivact.core.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains details about a standard export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardExportInfo {

    /**
     * The export's ID.
     */
    private String id;

    /**
     * The export's title.
     */
    private TranslatableString title;

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
