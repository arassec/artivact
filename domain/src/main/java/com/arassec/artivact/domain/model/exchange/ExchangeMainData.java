package com.arassec.artivact.domain.model.exchange;

import com.arassec.artivact.domain.model.TranslatableString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The file containing the main information about the exchanged data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeMainData {

    /**
     * Schema version for versioning exchange formats.
     */
    @Builder.Default
    private int schemaVersion = 1;

    /**
     * The export's ID.
     */
    private String id;

    /**
     * The title of the content.
     */
    private TranslatableString title;

    /**
     * A short description of the content.
     */
    private TranslatableString description;

    /**
     * A more detailed content description.
     */
    private TranslatableString content;

    /**
     * The type of the exchanged data.
     */
    private ContentSource contentSource;

    /**
     * The ID of the source of the exchanged content.
     */
    private String sourceId;

    /**
     * File extension of the cover picture (if present).
     */
    private String coverPictureExtension;

    /**
     * The configuration used to create this export.
     */
    private ExportConfiguration exportConfiguration;

}
