package com.arassec.artivact.domain.model.exchange;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains basic information about a collection export.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionExportInfo {

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
