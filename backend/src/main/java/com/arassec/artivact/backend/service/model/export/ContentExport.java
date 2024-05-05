package com.arassec.artivact.backend.service.model.export;

import com.arassec.artivact.backend.service.model.TranslatableString;
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
     * The export's description.
     */
    private TranslatableString description;

    /**
     * Timestamp of the last modification of the export.
     */
    private String lastModified;

}
