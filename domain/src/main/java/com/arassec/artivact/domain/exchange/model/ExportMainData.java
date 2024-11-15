package com.arassec.artivact.domain.exchange.model;

import com.arassec.artivact.core.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The file containing the main information about the export.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportMainData {

    /**
     * Schema version for versioning export formats.
     */
    @Builder.Default
    private int schemaVersion = 1;

    /**
     * The title of the content.
     */
    private TranslatableString title;

    /**
     * The description of the content.
     */
    private TranslatableString description;

    /**
     * The type of the export source.
     */
    private ExportSourceType exportSourceType;

    /**
     * The ID of the source of the exported content.
     */
    private String exportSourceId;

}
